package io.opentracing.contrib.jaxrs.server;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * @author Pavol Loffay
 */
public class OperationNameDecorator implements ServerOperationNameProvider {

    private final String operationName;
    private final ServerOperationNameProvider delegate;

    public OperationNameDecorator(String operationName, ServerOperationNameProvider operationNameProvider) {
        this.operationName = operationName;
        this.delegate = operationNameProvider;
    }

    @Override
    public String operationName(ContainerRequestContext containerRequestContext) {
        if (operationName != null) {
            return operationName;
        }

        return delegate.operationName(containerRequestContext);
    }
}
