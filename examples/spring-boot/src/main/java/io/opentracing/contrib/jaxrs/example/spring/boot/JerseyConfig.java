package io.opentracing.contrib.jaxrs.example.spring.boot;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.itest.common.rest.TestHandler;
import io.opentracing.contrib.jaxrs.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    @Inject
    public JerseyConfig(Tracer tracer) {
        Client client = ClientBuilder.newClient();

        ClientTracingFeature.Builder
                .traceAll(tracer, client)
                .build();

        register(ServerTracingDynamicFeature.Builder
                .traceAll(tracer)
                .withStandardTags()
                .build());

        register(new TestHandler(tracer, client));
    }
}
