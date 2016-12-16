package io.opentracing.contrib.jaxrs.server;

import javax.ws.rs.container.ContainerRequestContext;

/**
 * @author Pavol Loffay
 */
public class OperationNameAdapter implements OperationNameProvider {

    private final String operationName;
    private final OperationNameProvider delegate;

    public OperationNameAdapter(String operationName, OperationNameProvider operationNameProvider) {
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
