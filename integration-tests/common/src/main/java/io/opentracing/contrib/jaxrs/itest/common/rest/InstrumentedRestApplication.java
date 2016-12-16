package io.opentracing.contrib.jaxrs.itest.common.rest;

import static io.opentracing.contrib.jaxrs.itest.common.AbstractJettyTest.CLIENT_BUILDER_ATTRIBUTE;
import static io.opentracing.contrib.jaxrs.itest.common.AbstractJettyTest.TRACER_BUILDER_ATTRIBUTE;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import io.opentracing.contrib.jaxrs.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs.server.ServerTracingDynamicFeature;

/**
 * @author Pavol Loffay
 */
//@ApplicationPath("/")
public class InstrumentedRestApplication extends Application {

    private ClientTracingFeature.Builder clientTracingBuilder;
    private ServerTracingDynamicFeature.Builder serverTracingBuilder;

    public InstrumentedRestApplication(@Context ServletContext context) {
        this.serverTracingBuilder = (ServerTracingDynamicFeature.Builder) context.getAttribute(TRACER_BUILDER_ATTRIBUTE);
        this.clientTracingBuilder = (ClientTracingFeature.Builder) context.getAttribute(CLIENT_BUILDER_ATTRIBUTE);

        if (serverTracingBuilder == null || clientTracingBuilder == null) {
            throw new IllegalArgumentException("");
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
