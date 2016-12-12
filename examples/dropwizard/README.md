# Dropwizard Application Instrumented With OpenTracing

Dropwizard application instrumented with OpenTracing.

## Build & Run
```shell
$ mvn clean install
$ java -jar target/opentracing-jaxrs-example-dropwizard.jar server app.yml
```

## Example Requests
```shell
$ curl -ivX GET 'http://localhost:3000/dropwizard/bar'
$ curl -ivX GET 'http://localhost:3000/dropwizard/foo'
```
