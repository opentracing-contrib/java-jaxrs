package io.opentracing.contrib.jaxrs.example.swarm;

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

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs.client.ClientTracingFeature;
import io.opentracing.contrib.jaxrs.client.TracingProperties;
import io.opentracing.contrib.jaxrs.server.CurrentSpan;

/**
 * @author Pavol Loffay
 */
@Path("/")
public class HelloHandler {

    private Tracer tracer;
    private Client client;

    public HelloHandler(Tracer tracer) {
        this.tracer = tracer;
        this.client = ClientTracingFeature.Builder
                .traceAll(tracer, ResteasyClientBuilder.newClient())
                .build();
    }

    @GET
    @Path("/hello")
    public Response hello(@Context HttpHeaders headers) {
        return Response.status(Response.Status.OK).entity("/hello").build();
    }

    @GET
    @Path("/outgoing")
    public Response outgoing(@Context HttpServletRequest request) {

        // NOTE that this client request will be in different trace because parent span is not passed
        Response response = client.target("http://localhost:" + request.getServerPort() +
                request.getServletPath() + "/hello")
                .request()
                .property(TracingProperties.TRACING_DISABLED, true)
                .get();

        return Response.ok().entity(response.getEntity()).build();
    }

    @GET
    @Path("/outgoingNewThread")
    public Response outgoingNewThread(@Context HttpServletRequest request,
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
                        .property(TracingProperties.PARENT_SPAN, span)
                        .get();

                return (String)response.getEntity();
            }
        };

        Future<String> future = executor.submit(callable);
        executor.shutdown();

        return Response.ok().entity(future.get()).build();
    }

    @GET
    @Path("/async")
    public void async(@Suspended final AsyncResponse asyncResponse,
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
                    asyncResponse.resume("async finished");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}


