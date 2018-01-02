package test.java.com.nettyrpc.test.server;

import java.util.ArrayList;
import java.util.List;

import main.java.com.nettyrpc.server.RpcService;
import test.java.com.nettyrpc.test.client.Person;
import test.java.com.nettyrpc.test.client.PersonService;

/**
 * Created by luxiaoxun on 2016-03-10.
 */
@RpcService(PersonService.class)
public class PersonServiceImpl implements PersonService {

    @Override
    public List<Person> GetTestPerson(String name, int num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            persons.add(new Person(Integer.toString(i), name));
        }
        return persons;
    }
}
