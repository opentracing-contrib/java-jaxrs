package sk.loffay.opentracing.example.spring.boot;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import io.opentracing.Span;
import io.opentracing.Tracer;
import sk.loffay.opentracing.jax.rs.CurrentSpan;

/**
 * @author Pavol Loffay
 */
@Component
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

        return Response.ok().entity("/foo").build();
    }


    @GET
    @Path("/bar")
    public Response bar() {
        System.out.println("not traced");
        return Response.status(Response.Status.OK).entity("/bar").build();
    }
}
