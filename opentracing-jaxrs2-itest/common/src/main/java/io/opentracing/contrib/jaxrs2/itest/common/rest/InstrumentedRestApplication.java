package io.opentracing.contrib.jaxrs2.itest.common.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.itest.common.AbstractJettyTest;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
public class InstrumentedRestApplication extends Application {

    private Client client;
    private Tracer tracer;
    private ServerTracingDynamicFeature serverTracingFeature;

    public InstrumentedRestApplication(@Context ServletContext context) {
        this.serverTracingFeature = (ServerTracingDynamicFeature) context.getAttribute(
                AbstractJettyTest.SERVER_TRACING_FEATURE);
        this.client = (Client) context.getAttribute(AbstractJettyTest.CLIENT_ATTRIBUTE);
        this.tracer = (Tracer) context.getAttribute(AbstractJettyTest.TRACER_ATTRIBUTE);

        if (serverTracingFeature == null || client == null || tracer == null) {
            throw new IllegalArgumentException("Instrumented application is not initialized correctly. serverTracing:"
                    + serverTracingFeature + ", clientTracing: " + client);
        }
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<>();

        objects.add(serverTracingFeature);
        objects.add(new TestHandler(tracer, client));

        return Collections.unmodifiableSet(objects);
    }
}
