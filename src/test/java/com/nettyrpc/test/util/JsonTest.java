package test.java.com.nettyrpc.test.util;

import java.util.UUID;

import main.java.com.nettyrpc.protocol.JsonUtil;
import main.java.com.nettyrpc.protocol.RpcRequest;
import main.java.com.nettyrpc.protocol.RpcResponse;
import main.java.com.nettyrpc.protocol.SerializationUtil;
import test.java.com.nettyrpc.test.client.Person;
import test.java.com.nettyrpc.test.server.HelloServiceImpl;

/**
 * Created by jsc on 2016-03-10.
 */
public class JsonTest {
    public static void main(String[] args){
        RpcResponse response = new RpcResponse();
        response.setRequestId(UUID.randomUUID().toString());
        response.setError("Error msg");
        System.out.println(response.getRequestId());

        byte[] datas = JsonUtil.serialize(response);
        System.out.println("Json byte length: " + datas.length);

        byte[] datas2 = SerializationUtil.serialize(response);
        System.out.println("Protobuf byte length: " + datas2.length);

        RpcResponse resp = (RpcResponse)JsonUtil.deserialize(datas,RpcResponse.class);
        System.out.println(resp.getRequestId());
    }


    private static void TestJsonSerialize(){
        RpcRequest request = new RpcRequest();
        request.setClassName(HelloServiceImpl.class.getName());
        request.setMethodName(HelloServiceImpl.class.getDeclaredMethods()[0].getName());
        Person person = new Person("lu","xiaoxun");
        request.setParameters(new Object[]{person});
        request.setRequestId(UUID.randomUUID().toString());
        System.out.println(request.getRequestId());

        byte[] datas = JsonUtil.serialize(request);
        System.out.println("Json byte length: " + datas.length);

        byte[] datas2 = SerializationUtil.serialize(request);
        System.out.println("Protobuf byte length: " + datas2.length);

        RpcRequest req = (RpcRequest)JsonUtil.deserialize(datas,RpcRequest.class);
        System.out.println(req.getRequestId());
    }

}
