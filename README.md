[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing JAX-RS Instrumentation

OpenTracing instrumentation for JAX-RS standard.
It supports server and client request tracing.

Instrumentation by default adds set of standard tags and sets span operation name with HTTP method. 
This can be overridden by span decorators.

## Tracing Server Requests
```java
// register this in javax.ws.rs.core.Application
ServerTracingDynamicFeature.Builder
    .traceAll(yourPreferredTracer)
    .withDecorators(Arrays.asList(ServerSpanDecorator.HTTP_WILDCARD_PATH_OPERATION_NAME, 
                                  ServerSpanDecorator.STANDARD_TAGS))
    .build();

@GET
@Path("/hello")
@Traced(operationName = "helloRenamed") // optional, by default operation name is provided by ServerSpanDecorator
public Response hello(@BeanParam ServerSpanContext serverSpanContext) {
    /**
     * Some business logic
     */
    Span childSpan = tracer.buildSpan("businessOperation")
            .asChildOf(serverSpanContext.get())
            .start())
    childSpan.finish();

    return Response.status(Response.Status.OK).build();
}
```

## Tracing Client Requests
```java
ClientTracingFeature.Builder
    .traceAll(yourPreferredTracer, jaxRsClient)
    .withDecorators(Arrays.asList(ServerSpanDecorator.HTTP_PATH_OPERATION_NAME))
    .build();

Response response = jaxRsClient.target("http://localhost/endpoint")
    .request()
    .property(TracingProperties.CHILD_OF, parentSpanContext) // optional, by default new trace is started
    .property(TracingProperties.TRACING_DISABLED, false) // optional, by default false
    .get();
```

## Development
```shell
./mvnw clean install
```

## Release
Follow instructions in [RELEASE](RELEASE.md)


   [ci-img]: https://travis-ci.org/opentracing-contrib/java-jaxrs.svg?branch=master
   [ci]: https://travis-ci.org/opentracing-contrib/java-jaxrs
   [maven-img]: https://img.shields.io/maven-central/v/io.opentracing.contrib/opentracing-jaxrs2.svg?maxAge=2592000
   [maven]: http://search.maven.org/#search%7Cga%7C1%7Copentracing-jaxrs2
