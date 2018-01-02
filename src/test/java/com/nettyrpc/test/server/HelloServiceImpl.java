package test.java.com.nettyrpc.test.server;

import main.java.com.nettyrpc.server.RpcService;
import test.java.com.nettyrpc.test.client.HelloService;
import test.java.com.nettyrpc.test.client.Person;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
