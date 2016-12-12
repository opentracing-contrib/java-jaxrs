# Spring Boot Application Instrumented With OpenTracing

Spring Boot application instrumented with OpenTracing.

## Build & Run
```shell
$ mvn clean install
$ java -jar target/opentracing-jaxrs-example-spring-boot-exec.jar
```

## Example Requests
```shell
$ curl -ivX GET 'http://localhost:3000/spring-boot/jax-rs/bar'
$ curl -ivX GET 'http://localhost:3000/spring-boot/jax-rs/foo'
```
