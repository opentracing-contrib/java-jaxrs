# OpenTracing JAX-RS Instrumentation

Warning: This library is still a work in progress!

[![Travis](https://travis-ci.org/opentracing-contrib/java-jaxrs.svg?branch=master)](https://travis-ci.org/opentracing-contrib/java-jaxrs)

OpenTracing instrumentation for JAX-RS standard. 
It supports server and client request tracing.

## Tracing Server Requests
```
// register this in javax.ws.rs.core.Application
ServerTracingDynamicFeature.Builder
    .traceAll(yourPreferredTracer)
    .withStandardTags()
    .withDecorator(spanDecorator)
    .build();

@GET
@Path("/hello")
@Traced(operationName = "helloRenamed")     // optional, by default operation name is derived from request path
public Response hello(@BeanParam CurrentSpan currentSpan) {
    /**
     * Some business logic
    /*
    final Span span = currentSpan.injectedSpan();
    Span childSpan = tracer.buildSpan("businessOperation")
            .asChildOf(span)
            .start())
    childSpan.finish();

    return Response.status(Response.Status.OK).build();
}
```

## Tracing Client Requests
```
ClientTracingFeature.Builder
    .traceAll(tracer, jaxRsClient)
    .build();

Response response = jaxRsClient.target("http://localhost/endpoint")
    .request()
    .property(TracingProperties.CHILD_OF, parentSpan) // optional, by default new trace is started
    .property(TracingProperties.TRACING_DISABLED, false) // optional, by default false
    .get();
```
