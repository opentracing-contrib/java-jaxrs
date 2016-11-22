package sk.loffay.opentracing.example.swarm;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.opentracing.Span;
import io.opentracing.Tracer;
import sk.loffay.opentracing.jax.rs.CurrentSpan;
//import sk.loffay.opentracing.jax.rs.CurrentSpan;

/**
 * @author Pavol Loffay
 */
@Path("/")
public class HelloHandler {

    @Inject
    private Tracer tracer;

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

        return Response.status(Response.Status.OK).entity("/foo").build();
    }

    @GET
    @Path("/bar")
    public Response bar() {
        return Response.status(Response.Status.OK).entity("/bar").build();
    }
}
