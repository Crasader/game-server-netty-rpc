package main.java.com.nettyrpc.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import main.java.com.nettyrpc.protocol.RpcDecoder;
import main.java.com.nettyrpc.protocol.RpcEncoder;
import main.java.com.nettyrpc.protocol.RpcRequest;
import main.java.com.nettyrpc.protocol.RpcResponse;
import main.java.com.nettyrpc.registry.ServiceRegistry;

/**
 * RPC Server ：实现 RPC 服务器
 * Netty 可实现一个支持 NIO 的 RPC 服务器，需要使用ServiceRegistry注册服务地址
 * @author huangyong,luxiaoxun
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;
    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handlerMap = new HashMap<>();

    private static ThreadPoolExecutor threadPoolExecutor;//线程池

    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * 初始化服务器，注册多有的接口以及对应的实现类
     */
    @SuppressWarnings("unchecked")
	@Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
//        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
    	Map<String, Object> serviceBeanMap = ctx.getBeansOfType(RpcService.class);//加载所有被RpcService注解的类。
        if (MapUtils.isNotEmpty(serviceBeanMap)) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();//获取被注解的类所对应的接口名称
                handlerMap.put(interfaceName, serviceBean);//将接口类名称与实现类绑定
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //初始化管道：编解码，以及Rpc请求处理对象（初始Rpc处理的里的所有的注册的方法）
                    	@Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0))
                                    .addLast(new RpcDecoder(RpcRequest.class))//解码
                                    .addLast(new RpcEncoder(RpcResponse.class))//编码
                                    .addLast(new RpcHandler(handlerMap));// 处理 RPC 请求，反射
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            String[] array = serverAddress.split(":");//ip和端口号
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = bootstrap.bind(host, port).sync();
            logger.debug("Server started on port {}", port);

            if (serviceRegistry != null) {
            	//根据ip和端口号创建服务注册器：zookper（生成相应的节点）
            	serviceRegistry.register(serverAddress);
            }

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void submit(Runnable task){
        if(threadPoolExecutor == null){
            synchronized (RpcServer.class) {
                if(threadPoolExecutor == null){
                    threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }
}
