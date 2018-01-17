package main.java.com.nettyrpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * RPC annotation for RPC service
 *
 * @author huangyong
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component//把普通pojo实例化到spring容器中，相当于配置文件中的<bean id="" class=""/>
public @interface RpcService {
    Class<?> value();
}
