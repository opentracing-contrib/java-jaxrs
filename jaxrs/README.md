# OpenTracing JAX-RS Instrumentation

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
Client jaxRsClient = ClientTracingFeature.Builder
    .traceAll(tracer, jaxRsClient)
    .build();

Response response = jaxRsClient.target("http://localhost/enpoint")
    .request()
    .property(TracingProperties.PARENT_SPAN, parentSpan) // optional
    .property(TracingProperties.TRACING_DISABLED, false) // optional, by default false
    .get();
```
