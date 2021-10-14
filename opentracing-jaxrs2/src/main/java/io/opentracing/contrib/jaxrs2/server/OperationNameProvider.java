package io.opentracing.contrib.jaxrs2.server;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
      this.classMethod = clazz.getName()  + "." + method.getName();
    }

    @Override
    public String operationName(ContainerRequestContext requestContext) {
      return requestContext.getMethod() + ":" + classMethod;
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
        String classPath = extractPath(clazz);
        String methodPath = extractPath(method);
        if (classPath == null || methodPath == null) {
          for (Class<?> i: clazz.getInterfaces()) {
            if (classPath == null) {
              String intfPath = extractPath(i);
              if (intfPath != null) {
                classPath = intfPath;
              }
            }
            if (methodPath == null) {
              for (Method m: i.getMethods()) {
                if (Objects.equals(m.getName(), method.getName()) && Arrays.deepEquals(m.getParameterTypes(), method.getParameterTypes())) {
                  methodPath = extractPath(m);
                }
              }
            }
          }
        }
        return new WildcardOperationName(classPath == null ? "" : classPath, methodPath == null ? "" : methodPath);
      }
      private static String extractPath(AnnotatedElement element) {
        Path path = element.getAnnotation(Path.class);
        if (path != null) {
          return path.value();
        }
        return null;
      }
    }

    private final String path;

    WildcardOperationName(String clazz, String method) {
      if (clazz.isEmpty() || clazz.charAt(0) != '/') {
        clazz = "/" + clazz;
      }
      if (clazz.endsWith("/")) {
        clazz = clazz.substring(0, clazz.length() - 1);
      }
      if (method.isEmpty() || method.charAt(0) != '/') {
        method = "/" + method;
      }
      this.path = clazz + method;
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
