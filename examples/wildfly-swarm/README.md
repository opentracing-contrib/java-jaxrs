# Wildfly-Swarm Application Instrumented With OpenTracing

Wildfly Swarm application instrumented with OpenTracing.

## Build & Run
```shell
$ mvn clean install
$ java -jar target/opentracing-jaxrs-example-wildfly-swarm.jar
```

## Example Requests
```shell
$ curl -ivX GET 'http://localhost:3000/hello'
```

More requests can be found in `integration-tests/common/../../rest/TestHandler.java`
