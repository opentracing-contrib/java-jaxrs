package io.opentracing.contrib.jaxrs2.example.spring.boot;

import io.opentracing.Tracer;
import org.springframework.context.annotation.Bean;

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
