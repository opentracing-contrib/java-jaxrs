# Spring Boot Application Instrumented With OpenTracing

Spring Boot application instrumented with OpenTracing.

## Build & Run
```shell
$ mvn clean install
$ java -jar target/opentracing-jaxrs2-example-spring-boot-exec.jar
```

## Example Requests
```shell
$ curl -ivX GET 'http://localhost:3000/hello/1'
```

More requests can be found in [integration-tests/common/../rest/TestHandler.java](../../opentracing-jaxrs2-itest/common/src/main/java/io/opentracing/contrib/jaxrs2/itest/common/rest/TestHandler.java)
