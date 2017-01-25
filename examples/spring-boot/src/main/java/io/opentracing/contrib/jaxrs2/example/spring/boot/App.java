package io.opentracing.contrib.jaxrs2.example.spring.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @author Pavol Loffay
 */
@SpringBootApplication
public class App {
    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(App.class, args);
    }

}
