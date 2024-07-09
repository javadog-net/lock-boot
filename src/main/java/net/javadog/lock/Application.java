package net.javadog.lock;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpringBoot方式启动类
 *
 * @author javadog
 */
@SpringBootApplication
@MapperScan("net.javadog.lock.dao")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
