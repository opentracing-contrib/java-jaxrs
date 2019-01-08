package io.opentracing.contrib.jaxrs2.server;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Path;
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
   * resource method annotated with @Path("/foo/bar/{name: \\w+}") produces "/foo/bar/{name: \\w+}"
   *
   */
  class WildcardOperationName implements OperationNameProvider {
    static class Builder implements OperationNameProvider.Builder {
      @Override
      public OperationNameProvider build(Class<?> clazz, Method method) {
        String classPath = extractPath(clazz.getAnnotation(Path.class));
        if (classPath.endsWith("/")) {
          classPath = classPath.substring(0, classPath.length() - 1);
        }
        String methodPath = extractPath(method.getAnnotation(Path.class));
        return new WildcardOperationName(classPath + methodPath);
      }
      private static String extractPath(Path pathAnn) {
        String path = pathAnn == null ? "" : pathAnn.value();
        if (path.isEmpty() || path.charAt(0) != '/') {
          path = "/" + path;
        }
        return path;
      }
    }

    private final String path;

    WildcardOperationName(String path) {
      this.path = path;
    }

    @Override
    public String operationName(ContainerRequestContext requestContext) {
      return requestContext.getMethod() + ":" + path;
    }

    public static Builder newBuilder() {
      return new Builder();
    }
  }
}
