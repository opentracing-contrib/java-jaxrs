package io.opentracing.contrib.jaxrs2.itest.common.rest;

import io.opentracing.ActiveSpan;
import io.opentracing.NoopTracerFactory;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.TracingContext;
import java.net.URI;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
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
    @Path("/clientTracingChaining")
    public Response clientTracingEnabled(@Context HttpServletRequest request) throws ExecutionException, InterruptedException {
        assertActiveSpan();

        final int port = request.getServerPort();
        final String contextPath = request.getServletPath();

        client.target("http://localhost:" + port +
            contextPath + "/hello/1")
            .request()
            .get();

        return Response.ok().build();
    }

    @POST
    @Path("/postWithBody")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.TEXT_PLAIN)
    public Response postWithBody(@Context HttpServletRequest request, String body) throws ExecutionException, InterruptedException {
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
    public void async(@Suspended AsyncResponse asyncResponse, @BeanParam TracingContext tracingContext) {
//        assertActiveSpan(); // it's async do not assert here
        new Thread(new ExpensiveOperation(asyncResponse, tracingContext.spanContext()))
                .start();
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
    public Response exception(@Context HttpServletRequest request) throws Exception {
        assertActiveSpan();
        throw new IllegalStateException("error");
    }

    @GET
    @Path("/filtered")
    public Response filtered() {
        // Should never reach here.
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
            try(ActiveSpan expensiveOpSpan = tracer.buildSpan("expensiveOperation")
                    .asChildOf(parentContext).startActive()) {
                try {
                    Thread.sleep(random.nextInt(5));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                asyncResponse.resume((Object)null);
            }
        }
    }

    private void assertActiveSpan() {
        if (!(tracer == NoopTracerFactory.create())) {
            Assert.assertNotNull(tracer.activeSpan());
        }
    }
}


