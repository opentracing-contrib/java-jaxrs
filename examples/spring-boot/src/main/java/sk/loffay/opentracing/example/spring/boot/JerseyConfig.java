package sk.loffay.opentracing.example.spring.boot;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import io.opentracing.Tracer;
import sk.loffay.opentracing.jax.rs.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
@Component
@ApplicationPath("/jax-rs")
public class JerseyConfig extends ResourceConfig {

    @Inject
    public JerseyConfig(Tracer tracer) {
        register(new HelloHandler(tracer));
        register(ServerTracingDynamicFeature.Builder
                .traceAll(tracer)
                .withStandardTags()
                .build());
    }
}
