package io.opentracing.contrib.jaxrs.example.spring.boot;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.server.CurrentSpan;

/**
 * @author Pavol Loffay
 */
@Path("/")
public class HelloHandler {

    private Tracer tracer;

    public HelloHandler(Tracer tracer) {
        this.tracer = tracer;
    }

    @GET
    @Path("/foo")
    public Response foo(@BeanParam CurrentSpan currentSpan) {
        Span childSpan = tracer.buildSpan("child")
                .asChildOf(currentSpan.injectedSpan())
                .start();
        /**
         * Business logic
         */
        childSpan.finish();

        return Response.ok().entity("/foo").build();
    }

    @GET
    @Path("/bar")
    public Response bar() {
        return Response.status(Response.Status.OK).entity("/bar").build();
    }
}
