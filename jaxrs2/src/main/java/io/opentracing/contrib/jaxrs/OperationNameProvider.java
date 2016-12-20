package io.opentracing.contrib.jaxrs;

/**
 * This interface gives users a way to provide span operation name.
 * Operation name provided in {@link io.opentracing.contrib.jaxrs.server.Traced} takes precedence
 * (in case of server tracing).
 * Keep in mind that operation name can be also changed by {@link SpanDecorator}.
 *
 * @author Pavol Loffay
 */
public interface OperationNameProvider<T> {

    /**
     * Implement this to provide an operation name.
     *
     * @param type
     * @return operation name
     */
    String operationName(T type);
}
