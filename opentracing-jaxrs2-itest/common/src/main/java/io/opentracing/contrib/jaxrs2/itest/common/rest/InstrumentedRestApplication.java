package io.opentracing.contrib.jaxrs2.itest.common.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import io.opentracing.contrib.jaxrs2.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs2.itest.common.AbstractJettyTest;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
public class InstrumentedRestApplication extends Application {

    private ClientTracingFeature.Builder clientTracingBuilder;
    private ServerTracingDynamicFeature.Builder serverTracingBuilder;

    public InstrumentedRestApplication(@Context ServletContext context) {
        this.serverTracingBuilder = (ServerTracingDynamicFeature.Builder) context.getAttribute(
                AbstractJettyTest.TRACER_BUILDER_ATTRIBUTE);
        this.clientTracingBuilder = (ClientTracingFeature.Builder) context.getAttribute(
                AbstractJettyTest.CLIENT_BUILDER_ATTRIBUTE);

        if (serverTracingBuilder == null || clientTracingBuilder == null) {
            throw new IllegalArgumentException("Instrumented application is not initialized correctly. serverTracing:"
                    + serverTracingBuilder + ", clientTracing: " + clientTracingBuilder);
        }
    }

    @Override
    public Set<Object> getSingletons() {
        Set<Object> objects = new HashSet<>();

        clientTracingBuilder.build();

        objects.add(serverTracingBuilder.build());
        objects.add(new TestHandler(serverTracingBuilder.tracer(), clientTracingBuilder.client()));

        return Collections.unmodifiableSet(objects);
    }
}
