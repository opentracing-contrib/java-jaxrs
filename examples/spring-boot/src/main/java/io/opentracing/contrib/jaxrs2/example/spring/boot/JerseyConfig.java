package io.opentracing.contrib.jaxrs2.example.spring.boot;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature.Builder;
import io.opentracing.contrib.jaxrs2.itest.common.rest.TestHandler;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

/**
 * @author Pavol Loffay
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    @Inject
    public JerseyConfig(Tracer tracer) {
        Client client = ClientBuilder.newClient();
        client.register(new Builder(tracer).build());

        register(new ServerTracingDynamicFeature.Builder(tracer)
                .build());

        register(new TestHandler(tracer, client));
    }
}
