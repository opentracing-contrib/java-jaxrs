[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing JAX-RS Instrumentation

OpenTracing instrumentation for JAX-RS standard. It supports tracing of server and client requests.

Instrumentation by default adds a set of standard HTTP tags and as an operation name it uses a string defined in `@Path` annotation.
Custom tags or operation name can be added in span decorators.
It also traces writing response and requests bodies.

## Microprofile-OpenTracing
This implementation is also compatible with [Microprofile-OpenTracing](https://github.com/eclipse/microprofile-opentracing).
It can be used as a component inside Microprofile compatible application server. Note that
application servers have to add a few thing which are not provided by this component: CDI interceptor, 
automatically register tracing filters into client...

## Tracing server requests
Tracing server requests requires two components: JAX-RS dynamic feature and servlet filter.
Span is started in JAX-RS filter and finished in servlet filter.

By default OpenTracing provider and servlet filter can be automatically discovered and registered.
The only configuration that is required is to register a tracer instance via `GlobalTracer.register(tracer)`.
It can be done in `ServletContextListener`.

### Custom configuration
Custom configuration is supported:

```java
public class JaxRsApp extends javax.ws.rs.core.Application {

  @Override
  public Set<Object> getSingletons() {
    DynamicFeature tracing = new ServerTracingDynamicFeature.Builder(tracer)
        .withDecorators(decorators)
        .withSerializationDecorators(serializationDecorators)
        .build();

    return Collections.singleton(tracing);
  }
}
```

```java
@WebListener
public class OpenTracingContextInitializer implements javax.servlet.ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    ServletContext servletContext = servletContextEvent.getServletContext();
    Dynamic filterRegistration = servletContext
        .addFilter("tracingFilter", new SpanFinishingFilter(tracer));
    filterRegistration.setAsyncSupported(true);
    filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), false, "*");
  }
}
```

            
An example of traced REST endpoint:
```java
@GET
@Path("/hello")
@Traced(operationName = "helloRenamed") // optional, see javadoc
public Response hello() { // optional to get server span context

  // this span will be ChildOf of span representing server request processing
  Span childSpan = tracer.buildSpan("businessOperation")
          .start())

   // business logic
  childSpan.finish();

  return Response.status(Response.Status.OK).build();
}
```

## Tracing client requests
```java
Client client = ClientBuilder.newBuilder()
  .reqister(ClientTracingFeature.class)
  .build();

Response response = client.target("http://localhost/endpoint")
  .request()
  .property(TracingProperties.CHILD_OF, parentSpanContext) // optional, by default new parent is inferred from span source
  .property(TracingProperties.TRACING_DISABLED, false) // optional, by default everything is traced
  .get();
```

### Async
Async requests are executed in a different thread than when the client has been invoked, therefore
spans representing client requests are not connected to appropriate parent. To fix this JAX-RS client
has to use OpenTracing-aware [`ExecutorService`](https://github.com/opentracing-contrib/java-concurrent).

#### Jersey
```java
@ClientAsyncExecutor
public class DelegateExecutorServiceProvider implements ExecutorServiceProvider {

  private final ExecutorService executorService;

  public DelegateExecutorServiceProvider(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public ExecutorService getExecutorService() {
    return executorService;
  }

  @Override
  public void dispose(ExecutorService executorService) {
  }
}

Client client = ClientBuilder.newBuilder()
    .register(new DelegateExecutorServiceProvider(
        new TracedExecutorService(Executors.newFixedThreadPool(8), tracer)))
    ...
```

#### RestEasy
```java
Client client = new ResteasyClientBuilder()
    .asyncExecutor(new TracedExecutorService(Executors.newFixedThreadPool(8), tracer))
    ...
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
   
   ## License

[Apache 2.0 License](./LICENSE).
