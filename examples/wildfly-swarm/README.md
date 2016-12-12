# Wildfly-Swarm Application Instrumented With OpenTracing

Wildfly Swarm application instrumented with OpenTracing.

## Build & Run
```shell
$ mvn clean install
$ java -jar target/opentracing-jaxrs-example-wildfly-swarm.jar -Dswarm.http.port=3000 -Dswarm.logging=TRACE
```

## Example Requests
```shell
$ curl -ivX GET 'http://localhost:3000/wildfly-swarm/hello'
$ curl -ivX GET 'http://localhost:3000/wildfly-swarm/outgoing'
$ curl -ivX GET 'http://localhost:3000/wildfly-swarm/outgoingNewThread'
$ curl -ivX GET 'http://localhost:3000/wildfly-swarm/async'
```
