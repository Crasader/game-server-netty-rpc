package main.java.com.nettyrpc.client.proxy;

import main.java.com.nettyrpc.client.RPCFuture;

/**
 * Created by luxiaoxun on 2016/3/16.
 */
public interface IAsyncObjectProxy {
    public RPCFuture call(String funcName, Object... args);
}