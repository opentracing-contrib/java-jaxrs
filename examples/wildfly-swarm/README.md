# Wildfly-Swarm Application Instrumented With OpenTracing

Wildfly Swarm application instrumented with OpenTracing.

## Build & Run
```shell
$ mvn clean install
$ java -jar target/opentracing-integration-example-wildfly-swarm.jar -Dswarm.http.port=3000
```

## Example Requests
```shell
$ curl -ivX GET 'http://localhost:3000/wildfly-swarm/bar'
$ curl -ivX GET 'http://localhost:3000/wildfly-swarm/foo'
```
