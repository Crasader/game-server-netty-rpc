package main.java.com.nettyrpc.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * RPC Encoder
 * 使用RpcEncoder提供 RPC 编码，只需扩展 Netty 的MessageToByteEncoder抽象类的encode方法即可
 * @author huangyong
 */
public class RpcEncoder extends MessageToByteEncoder {
	//需要编码成的类（对象）--->将对象根据编码方式进行序列化成字节数组
    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if (genericClass.isInstance(in)) {
            byte[] data = SerializationUtil.serialize(in);
            //byte[] data = JsonUtil.serialize(in); // Not use this, have some bugs
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}
