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
@Component// �����ɱ� Spring ɨ��
public @interface RpcService {
    Class<?> value();
}
