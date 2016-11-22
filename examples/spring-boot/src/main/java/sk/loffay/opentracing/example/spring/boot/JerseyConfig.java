package sk.loffay.opentracing.example.spring.boot;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import sk.loffay.opentracing.jax.rs.SpanExtractRequestFilter;
import sk.loffay.opentracing.jax.rs.SpanFinishResponseFilter;

/**
 * @author Pavol Loffay
 */
@Component
@ApplicationPath("/jax-rs")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        register(HelloHandler.class);
        register(SpanExtractRequestFilter.class);
        register(SpanFinishResponseFilter.class);
    }
}
