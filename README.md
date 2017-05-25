[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing JAX-RS Instrumentation

OpenTracing instrumentation for JAX-RS standard. It supports tracing of server and client requests.

Instrumentation by default adds a set of standard HTTP tags and as an operation name it uses a string defined in `@Path` annotation. Custom tags or operation name can be defined in span decorators.

## Tracing Server Requests
By default OpenTracing provider is automatically discovered and registered. The only configuration that is required is to register a tracer instance: `GlobalTracer.register(tracer)` at application startup.

Custom configuration 
```java
// code sample from javax.ws.rs.core.Application#getSingletons();
DynamicFeature tracing = new ServerTracingDynamicFeature.Builder(tracer)
    .withDecorators(decorators)
    .build();
singletons.add(tracing);
return singletons;
```
            
An example of traced REST endpoint:
```java
@GET
@Path("/hello")
@Traced(operationName = "helloRenamed") // optional, see javadoc
public Response hello(@BeanParam ServerSpanContext serverSpanContext) { // optional to get server span context
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
Client client = ClientBuilder.newClient();
client.register(ClientTracingFeature.class);

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
