package sk.loffay.opentracing.example.spring.boot;

import org.hawkular.apm.client.opentracing.APMTracer;
import org.springframework.context.annotation.Bean;

import io.opentracing.Tracer;

/**
 * @author Pavol Loffay
 */
@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public Tracer tracer() {
        return new APMTracer();
    }
}
