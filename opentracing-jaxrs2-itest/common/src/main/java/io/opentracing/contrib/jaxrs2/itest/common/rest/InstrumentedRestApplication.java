package io.opentracing.contrib.jaxrs2.itest.common.rest;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.itest.common.AbstractJettyTest;
import io.opentracing.contrib.jaxrs2.server.ServerTracingDynamicFeature;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

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
        objects.add(new DisabledTestHandler(tracer));
        objects.add(new DenyFilteredFeature());
        objects.add(new MappedExceptionMapper());

        return Collections.unmodifiableSet(objects);
    }

    /**
     * A dynamic feature that introduces a request filter denying all requests to "filtered"
     * endpoints.
     *
     * @author Maxime Petazzoni
     */
    private static final class DenyFilteredFeature implements DynamicFeature {
        @Override
        public void configure(ResourceInfo resourceInfo, FeatureContext context) {
            context.register(new ContainerRequestFilter() {
                @Override
                public void filter(ContainerRequestContext requestContext) throws IOException {
                    if (requestContext.getUriInfo().getPath().endsWith("filtered")) {
                        throw new ForbiddenException();
                    }
                }
            }, Priorities.AUTHORIZATION);
        }
    }

    public static class MappedException extends WebApplicationException {
    }

    private static class MappedExceptionMapper implements ExceptionMapper<MappedException> {
        @Override
        public Response toResponse(MappedException exception) {
            return Response.status(405).build();
        }
    }
}
