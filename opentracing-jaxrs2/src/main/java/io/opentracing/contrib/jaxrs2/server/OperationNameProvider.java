package io.opentracing.contrib.jaxrs2.server;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author Pavol Loffay
 */
public interface OperationNameProvider {
  interface Builder {
    OperationNameProvider build(Class<?> clazz, Method method);
  }

  String operationName(ContainerRequestContext requestContext);

  /**
   * Returns HTTP method as operation name
   */
  class HTTPMethodOperationName implements OperationNameProvider {
    static class Builder implements OperationNameProvider.Builder {
      @Override
      public OperationNameProvider build(Class<?> clazz, Method method) {
        return new HTTPMethodOperationName();
      }
    }
    HTTPMethodOperationName() {
    }

    @Override
    public String operationName(ContainerRequestContext requestContext) {
      return requestContext.getMethod();
    }

    public static Builder newBuilder() {
      return new Builder();
    }
  }

  /**
   * Default Microprofile operation name <HTTP method>:<package name>.<Class name>.<method name>
   */
  class ClassNameOperationName implements OperationNameProvider {
    static class Builder implements OperationNameProvider.Builder {
      @Override
      public OperationNameProvider build(Class<?> clazz, Method method) {
        return new ClassNameOperationName(clazz, method);
      }
    }

    private String classMethod;
    ClassNameOperationName(Class<?> clazz, Method method) {
      this.classMethod = String.format("%s.%s", clazz.getName() , method.getName());
    }

    @Override
    public String operationName(ContainerRequestContext requestContext) {
      return String.format("%s:%s", requestContext.getMethod(), classMethod);
    }

    public static Builder newBuilder() {
      return new Builder();
    }
  }

  /**
   * As operation name provides "wildcard" HTTP path e.g:
   *
   * resource method annotated with @Path("/foo/bar/{name: \\w+}") produces "/foo/bar/{name}"
   *
   */
  class WildcardOperationName implements OperationNameProvider {
    static class Builder implements OperationNameProvider.Builder {
      @Override
      public OperationNameProvider build(Class<?> clazz, Method method) {
        return new WildcardOperationName();
      }
    }

    WildcardOperationName() {
    }

    @Override
    public String operationName(ContainerRequestContext requestContext) {
      MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();
      String path = requestContext.getUriInfo().getPath();
      if (path.isEmpty() || path.charAt(0) != '/') {
        path = "/" + path;
      }
      for (Map.Entry<String, List<String>> entry: pathParameters.entrySet()) {
        final String originalPathFragment = String.format("{%s}", entry.getKey());

        for (String currentPathFragment: entry.getValue()) {
          path = path.replace(currentPathFragment, originalPathFragment);
        }
      }
      return path;
    }

    public static Builder newBuilder() {
      return new Builder();
    }
  }
}
