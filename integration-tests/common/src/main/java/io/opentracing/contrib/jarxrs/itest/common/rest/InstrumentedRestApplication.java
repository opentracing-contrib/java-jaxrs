package io.opentracing.contrib.jarxrs.itest.common.rest;

import static io.opentracing.contrib.jarxrs.itest.common.AbstractJettyTest.CLIENT_ATTRIBUTE;
import static io.opentracing.contrib.jarxrs.itest.common.AbstractJettyTest.TRACER_ATTRIBUTE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
//@ApplicationPath("/")
public class InstrumentedRestApplication extends Application {

    private Client client;
    private Tracer tracer;

    public InstrumentedRestApplication(@Context ServletContext context) {
        this.tracer = (Tracer) context.getAttribute(TRACER_ATTRIBUTE);
        this.client = (Client) context.getAttribute(CLIENT_ATTRIBUTE);
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<>();

        objects.add(ServerTracingDynamicFeature.Builder
                .traceAll(tracer)
                .build());

        ClientTracingFeature.Builder
            .traceAll(tracer, client)
            .build();

        objects.add(new TestHandler(tracer, client));

        return Collections.unmodifiableSet(objects);
    }
}
