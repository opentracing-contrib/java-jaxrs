<<<<<<< 9e9343cb10da39a6dece3cde9a1b80f0b6c050fc
<<<<<<< 2e3d67d13f634234704e43397c60e4d52016db3e
<<<<<<< 577365b6e59a9a2b062a890c6d85e55d2042b92e
# java-jaxrs
=======
# OpenTracing Java Integrations
=======
# OpenTracing JAX-RS 2.0 Instrumentation
>>>>>>> Change groupid to io.opentracing.contrib
=======
# OpenTracing JAX-RS Instrumentation
>>>>>>> integration tests for jersey, resteasy and apache cxf

Warning: This library is still a work in progress!

[![Travis](https://travis-ci.org/pavolloffay/opentracing-java-integrations.svg?branch=master)](https://travis-ci.org/pavolloffay/opentracing-java-integrations)

<<<<<<< 9e9343cb10da39a6dece3cde9a1b80f0b6c050fc
<<<<<<< 2e3d67d13f634234704e43397c60e4d52016db3e

This repository will provide instrumentation of several Java frameworks using OpenTracing API.
Consumers of this libraries will provide OpenTracing API implementation, therefore this 
instrumentation does not depend on any tracer implementation.

## Supported Frameworks
* JAX-RS
>>>>>>> Prapagate current span in request properties not in threadlocal
=======
This repository provides instrumentation for JAX-RS API.
>>>>>>> Change groupid to io.opentracing.contrib
=======
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

Response response = jaxRsClient.target("http://localhost/enpoint")
    .request()
    .property(TracingProperties.CHILD_OF, parentSpan) // optional, by default new trace is started
    .property(TracingProperties.TRACING_DISABLED, false) // optional, by default false
    .get();
```
>>>>>>> integration tests for jersey, resteasy and apache cxf
