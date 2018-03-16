package io.opentracing.contrib.jaxrs2.itest.common.rest;

import io.opentracing.Scope;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.itest.common.rest.InstrumentedRestApplication.MappedException;
import io.opentracing.noop.NoopTracer;
import java.net.URI;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.opentracing.Traced;
import org.junit.Assert;

/**
 * @author Pavol Loffay
 */
@Path("/")
public class TestHandler {

    private Tracer tracer;
    private Client client;

    public TestHandler(Tracer tracer, Client client) {
        this.tracer = tracer;
        this.client = client;
    }

    @GET
    @Path("/")
    public Response root() {
        assertActiveSpan();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/hello/{id}")
    public Response helloMethod(@Context HttpHeaders headers, @PathParam("id") String id) {
        assertActiveSpan();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/operation")
    @Traced(operationName = "renamedOperation")
    public Response operation(@Context HttpHeaders headers) {
        assertActiveSpan();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/tracedFalseIn")
    @Traced(operationName = "renamedOperation", value = false)
    public Response tracedFalse() {
        assertNoActiveSpan();
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/clientTracingChaining")
    public Response clientTracingEnabled(@Context HttpServletRequest request) {
        assertActiveSpan();

        final int port = request.getServerPort();
        final String contextPath = request.getContextPath();

        client.target("http://localhost:" + port +
            contextPath + "/hello/1")
            .request()
            .get()
            .close();
        return Response.ok().build();
    }

    @POST
    @Path("/postWithBody")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postWithBody(@Context HttpServletRequest request, String body) {
        assertActiveSpan();
        return Response.ok().entity(body).build();
    }

    @GET
    @Path("/path/{pathParam}")
    public Response pathParam(@PathParam("pathParam") String pathParam1) {
        assertActiveSpan();
        return Response.ok().build();
    }

    @GET
    @Path("/path/{pathParam}/path2/{pathParam2}")
    public Response pathParam(@PathParam("pathParam") String pathParam1,
                              @PathParam("pathParam2") String pathParam2) {
        assertActiveSpan();
        return Response.ok().build();
    }

    @GET
    @Path("/path/{pathParam}/path/{regexParam: \\w+}")
    public Response pathParamRegex(@PathParam("pathParam") String pathParam,
                                   @PathParam("regexParam") String regexParam) {
        assertActiveSpan();
        return Response.ok().build();
    }

    @GET
    @Path("/async")
    public void async(@Suspended AsyncResponse asyncResponse) {
        assertActiveSpan();
        new Thread(new ExpensiveOperation(asyncResponse, tracer.scopeManager().active().span().context()))
                .start();
    }

    @GET
    @Path("/asyncError")
    public void asyncError(@Suspended final AsyncResponse asyncResponse) {
        assertActiveSpan();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // this exception is not propagated to AsyncListener
                    asyncResponse.resume(new RuntimeException("asyncError"));
                }
            }
        }).start();
        throw new IllegalStateException();
    }

    @GET
    @Path("/redirect")
    public Response redirect(@Context HttpServletRequest request) {
        assertActiveSpan();
        String url = String.format("localhost:%d/%s/hello/1", request.getLocalPort(),
            request.getContextPath()).replace("//", "/");
        return Response.seeOther(URI.create("http://" + url)).build();
    }

    @GET
    @Path("/exception")
    public Response exception(@Context HttpServletRequest request) {
        assertActiveSpan();
        throw new IllegalStateException("error");
    }

    @GET
    @Path("/mappedException")
    public Response customException(@Context HttpServletRequest request) {
        assertActiveSpan();
        throw new MappedException();
    }

    @GET
    @Path("/filtered")
    public Response filtered() {
        // Should never reach here.
        return Response.ok().build();
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok().build();
    }

    private class ExpensiveOperation implements Runnable {

        private AsyncResponse asyncResponse;
        private Random random;
        private SpanContext parentContext;

        public ExpensiveOperation(AsyncResponse asyncResponse, SpanContext parentContext) {
            this.asyncResponse = asyncResponse;
            this.random = new Random();
            this.parentContext = parentContext;
        }

        @Override
        public void run() {
            try(Scope expensiveOpSpan = tracer.buildSpan("expensiveOperation")
                    .asChildOf(parentContext).startActive(true)) {
                try {
                    Thread.sleep(random.nextInt(5));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                asyncResponse.resume(Response.ok().build());
            }
        }
    }

    private void assertNoActiveSpan() {
        if (!(tracer instanceof NoopTracer)) {
            Assert.assertNull(tracer.scopeManager().active());
        }
    }

    private void assertActiveSpan() {
        if (!(tracer instanceof NoopTracer)) {
            Assert.assertNotNull(tracer.scopeManager().active());
        }
    }
}


