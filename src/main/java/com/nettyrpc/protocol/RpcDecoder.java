package main.java.com.nettyrpc.protocol;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * RPC Decoder
 * RpcDecoder提供 RPC 解码，只需扩展 Netty 的ByteToMessageDecoder抽象类的decode方法即可
 * @author huangyong
 */
public class RpcDecoder extends ByteToMessageDecoder {

	//需要解码成的类（对象）--->将序列化的数据解码成一个对象
    private Class<?> genericClass;//generic:adj. 类的；一般的；属的；非商标的

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    public final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        in.markReaderIndex();
        int dataLength = in.readInt();
        /*if (dataLength <= 0) {
            ctx.close();
        }*/
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        Object obj = SerializationUtil.deserialize(data, genericClass);
        //Object obj = JsonUtil.deserialize(data,genericClass); // Not use this, have some bugs
        out.add(obj);
    }

}
