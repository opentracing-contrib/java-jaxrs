[![Build Status][ci-img]][ci] [![Released Version][maven-img]][maven]

# OpenTracing JAX-RS Instrumentation

OpenTracing instrumentation for JAX-RS standard. It supports tracing of server and client requests.

Instrumentation by default adds a set of standard HTTP tags and as an operation name it uses a string defined in `@Path` annotation.
Custom tags or operation name can be added via span decorators.
This instrumentation also supports tracing of (de)serialization of response and requests bodies.

## MicroProfile-OpenTracing
This implementation is compatible with [MicroProfile-OpenTracing (MP-OT)](https://github.com/eclipse/microprofile-opentracing).
It can be used as a building block of MicroProfile compatible application server. Note that
application servers have to add a few things which are not provided by this project: CDI interceptor, 
automatically register tracing filters into client... [SmallRye-OpenTracing](https://github.com/smallrye/smallrye-opentracing)
uses this library to provide a vendor neutral implementation of MP-OT.

## Tracing server requests
Tracing server requests requires two components: JAX-RS dynamic feature and servlet filter.
Span is started in JAX-RS filter and finished in servlet filter.

## Auto discovery
Tracing can be automatically enabled by adding the following dependency on classpath.
This mechanism requires a tracer to be registered in `GlobalTracer`. This is typically done in
`ServletContextListener`. Note that JAX-RS clients are not automatically instrumented. Client tracing
feature has to be explicitly registered to all client instances.

```xml
<dependency>
  <groupId>io.opentracing.contrib</groupId>
  <artifactId>opentracing-jaxrs2-discovery</artifactId>
</dependency>
```

### Custom configuration
For custom configuration use the following dependency:
```xml
<dependency>
  <groupId>io.opentracing.contrib</groupId>
  <artifactId>opentracing-jaxrs2</artifactId>
</dependency>
```

The Custom configuration can be achieved by adding `ServerTracingDynamicFeature` to `Application.singletons` or by wrapping the feature with a class annotated with `@Provider`. 
This approach does not require adding all classes to singletons set.

Dynamic feature registration via custom provider:
```java
@Provider
public class TracingInitializer implements DynamicFeature {

  private final ServerTracingDynamicFeature serverTracingDynamicFeature =
      new ServerTracingDynamicFeature.Builder(GlobalTracer.get())
          .withOperationNameProvider(ClassNameOperationName.newBuilder())
      .build();

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    serverTracingDynamicFeature.configure(resourceInfo, context);
  }
}

```

Dynamic feature registration via singletons:
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

Filter registration:
```java
@WebListener
public class OpenTracingContextInitializer implements javax.servlet.ServletContextListener {

  @Override
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    io.opentracing.tracer tracer = new ....
    GlobalTracer.register(tracer); // or preferably use CDI
    
    ServletContext servletContext = servletContextEvent.getServletContext();
    Dynamic filterRegistration = servletContext
        .addFilter("tracingFilter", new SpanFinishingFilter());
    filterRegistration.setAsyncSupported(true);
    filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), false, "*");
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
