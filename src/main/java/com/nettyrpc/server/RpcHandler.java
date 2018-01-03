package main.java.com.nettyrpc.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import main.java.com.nettyrpc.protocol.RpcRequest;
import main.java.com.nettyrpc.protocol.RpcResponse;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
 * RPC Handler（RPC request processor）
 * 使用RpcHandler中处理 RPC 请求，只需扩展 Netty 的SimpleChannelInboundHandler抽象类即可
 * Java 反射带来的性能问题，我们可以使用 CGLib 提供的反射 API，如上面用到的FastClass与FastMethod
 * @author luxiaoxun
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }
    
    /**
     * 解码客户端请求数据后生成RpcRequest对象，进行调用该方法，调用后续的处理逻辑获取结果
     */
    @Override
    public void channelRead0(final ChannelHandlerContext ctx,final RpcRequest request) throws Exception {
        RpcServer.submit(new Runnable() {
            @Override
            public void run() {
                logger.debug("Receive request " + request.getRequestId());
                RpcResponse response = new RpcResponse();
                response.setRequestId(request.getRequestId());
                try {
                    Object result = handle(request);
                    response.setResult(result);
                } catch (Throwable t) {
                    response.setError(t.toString());
                    logger.error("RPC Server handle request error",t);
                }
                ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        logger.debug("Send response for request " + request.getRequestId());
                    }
                });
            }
        });
    }

    /**
     * 根据请求数据，获取请求的类以及对应的方法，使用Cglib的API获取方法对象，调用-->返回结果
     * @param request
     * @return
     * @throws Throwable
     */
    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();//请求类名
        Object serviceBean = handlerMap.get(className);//获取请求类的实现对象

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();//请求方法名
        Class<?>[] parameterTypes = request.getParameterTypes();//请求方法参数类型
        Object[] parameters = request.getParameters();//请求方法参数值

        logger.debug(serviceClass.getName());
        logger.debug(methodName);
        for (int i = 0; i < parameterTypes.length; ++i) {
            logger.debug(parameterTypes[i].getName());
        }
        for (int i = 0; i < parameters.length; ++i) {
            logger.debug(parameters[i].toString());
        }

        // JDK reflect
        /*Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);*/

        // Cglib reflect
        //避免使用 Java 反射带来的性能问题，我们可以使用 CGLib 提供的反射 API，如上面用到的FastClass与FastMethod
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
