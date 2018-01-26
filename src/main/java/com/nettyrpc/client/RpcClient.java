package main.java.com.nettyrpc.client;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import main.java.com.nettyrpc.client.proxy.IAsyncObjectProxy;
import main.java.com.nettyrpc.client.proxy.ObjectProxy;
import main.java.com.nettyrpc.registry.ServiceDiscovery;

/**
 * RPC Client锛圕reate RPC proxy锛�
 * 使用 Java 提供的动态代理技术实现 RPC 代理（当然也可以使用 CGLib 来实现）
 * @author luxiaoxun
 */
public class RpcClient {

    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

    public RpcClient(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcClient(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 创建代理对象
     * @param interfaceClass
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),//类加载器（java的动态代理会动态（在运行时）生成代理对象的class文件），故需要类加载器加载class文件
                new Class<?>[]{interfaceClass},//需要代理的接口类
                new ObjectProxy<T>(interfaceClass)//处理的handler（实现了InvocationHandler），将会调用invoke方法进行远程调用
        );
    }

    public static <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass);
    }

    public static void submit(Runnable task){
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectManage.getInstance().stop();
    }
    
    
    
    
//    @SuppressWarnings("unchecked")
//    public <T> T create(Class<?> interfaceClass) {
//        return (T) Proxy.newProxyInstance(
//            interfaceClass.getClassLoader(),
//            new Class<?>[]{interfaceClass},
//            new InvocationHandler() {
//                @Override
//                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                    RpcRequest request = new RpcRequest(); // 创建并初始化 RPC 请求
//                    request.setRequestId(UUID.randomUUID().toString());
//                    request.setClassName(method.getDeclaringClass().getName());
//                    request.setMethodName(method.getName());
//                    request.setParameterTypes(method.getParameterTypes());
//                    request.setParameters(args);
//
//                    if (serviceDiscovery != null) {
//                        serverAddress = serviceDiscovery.discover(); // 发现服务
//                    }
//
//                    String[] array = serverAddress.split(":");
//                    String host = array[0];
//                    int port = Integer.parseInt(array[1]);
//
//                    RpcClient client = new RpcClient(host, port); // 初始化 RPC 客户端
//                    RpcResponse response = client.send(request); // 通过 RPC 客户端发送 RPC 请求并获取 RPC 响应
//
//                    if (response.isError()) {
//                        throw response.getError();
//                    } else {
//                        return response.getResult();
//                    }
//                }
//            }
//        );
//    }
    
}

