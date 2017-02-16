package io.opentracing.contrib.jaxrs2.example.spring.boot;

import org.springframework.context.annotation.Bean;

import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public Tracer tracer() {
        return new LoggingTracer();
    }
}
