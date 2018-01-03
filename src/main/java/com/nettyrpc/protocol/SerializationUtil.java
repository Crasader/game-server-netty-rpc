package main.java.com.nettyrpc.protocol;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * Serialization Util锛圔ased on Protostuff锛�
 * 如需要替换其它序列化框架，只需修改SerializationUtil即可。
 * 当然，更好的实现方式是提供配置项来决定使用哪种序列化方式
 * @author huangyong
 */
public class SerializationUtil {

    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();
    //使用 Objenesis 来实例化对象，它是比 Java 反射更加强大。
    private static Objenesis objenesis = new ObjenesisStd(true);

    private SerializationUtil() {
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    /**
     * 搴忓垪鍖栵紙瀵硅薄 -> 瀛楄妭鏁扮粍锛�
     * 讲对象按照编码方式序列化成字节数组
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();//获得对象的类；
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);//使用LinkedBuffer分配一块默认大小的buffer空间；
        try {
            Schema<T> schema = getSchema(cls);//通过对象的类构建对应的schema；
            
//            ByteArrayOutputStream outputStream=new ByteArrayOutputStream();
//            ProtostuffIOUtil.writeTo(outputStream,obj,schema,buffer);
            
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);//使用给定的schema将对象序列化为一个byte数组，并返回。
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 鍙嶅簭鍒楀寲锛堝瓧鑺傛暟缁� -> 瀵硅薄锛�
     * 将序列化数据解码反射成对象，并返回实例对象
     */
    @SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = (T) objenesis.newInstance(cls);//使用 Objenesis 来实例化对象，它是比 Java 反射更加强大。
            Schema<T> schema = getSchema(cls);//Schema:模式；计划；图解；概要--->通过对象的类构建对应的schema；
            ProtostuffIOUtil.mergeFrom(data, message, schema);//使用给定的schema将byte数组和对象合并，并返回。
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
