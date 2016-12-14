package io.opentracing.contrib.jaxrs.example.swarm;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Application;

import org.hawkular.apm.client.opentracing.APMTracer;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.itest.common.rest.TestHandler;
import io.opentracing.contrib.jaxrs.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
@ApplicationPath("/")
public class RestApplication extends Application {

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<>();

        Tracer tracer = new APMTracer();

        objects.add(ServerTracingDynamicFeature.Builder
                .traceAll(tracer)
                .build());

        Client client = ClientBuilder.newClient();
        ClientTracingFeature.Builder
                .traceAll(tracer, client)
                .withStandardTags()
                .build();

        objects.add(new TestHandler(tracer, client));
        return Collections.unmodifiableSet(objects);
    }
}
