package io.opentracing.contrib.jarxrs.itest.common.rest;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.client.TracingProperties;
import io.opentracing.contrib.jaxrs.server.CurrentSpan;
import io.opentracing.contrib.jaxrs.server.Traced;

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
    @Path("/hello")
    public Response helloMethod(@Context HttpHeaders headers) {
        return Response.status(Response.Status.OK).entity("/hello").build();
    }

    @GET
    @Path("/operation")
    @Traced(operationName = "renamedOperation")
    public Response operation(@Context HttpHeaders headers) {
        return Response.status(Response.Status.OK).build();
    }

    @GET
    @Path("/clientTracingDisabled")
    public Response clientTracingDisabled(@Context HttpServletRequest request) {

        // NOTE that this client request will be in different trace because parent span is not passed
        Response response = client.target("http://localhost:" + request.getServerPort() +
                request.getServletPath() + "/hello")
                .request()
                .property(TracingProperties.TRACING_DISABLED, true)
                .get();

        Object entity = response.getEntity();
        response.close();

        return Response.ok().entity(entity).build();
    }

    @GET
    @Path("/clientTracingEnabled")
    public Response clientTracingEnabled(@Context HttpServletRequest request,
                                         @BeanParam CurrentSpan currentSpan) throws ExecutionException, InterruptedException {

        final int port = request.getServerPort();
        final String contextPath = request.getServletPath();
        final Span span = currentSpan.injectedSpan();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() {
                Response response = client.target("http://localhost:" + port +
                        contextPath + "/hello")
                        .request()
                        .property(TracingProperties.CHILD_OF, span)
                        .get();

                String entity = response.readEntity(String.class);
                response.close();
                return entity;
            }
        };

        Future<String> future = executor.submit(callable);
        executor.shutdown();

        return Response.ok().entity(future.get()).build();
    }

    @GET
    @Path("/async")
    public void async(@Suspended AsyncResponse asyncResponse,
                      @BeanParam CurrentSpan currentSpan) {

        final Span serverSpan = currentSpan.injectedSpan();

        new Thread(new ExpensiveOperation(serverSpan, asyncResponse))
                .start();
    }

    private class ExpensiveOperation implements Runnable {

        private Span parentSpan;
        private AsyncResponse asyncResponse;
        private Random random;

        public ExpensiveOperation(Span span, AsyncResponse asyncResponse) {
            this.parentSpan = span;
            this.asyncResponse = asyncResponse;
            this.random = new Random();
        }

        @Override
        public void run() {
            try(Span expensiveOpSpan = tracer.buildSpan("expensiveOperation")
                    .asChildOf(parentSpan)
                    .start()) {
                try {
                    Thread.sleep(random.nextInt(5));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                asyncResponse.resume("async finished");
            }
        }
    }
}


