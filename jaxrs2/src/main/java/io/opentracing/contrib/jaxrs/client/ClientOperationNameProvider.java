package io.opentracing.contrib.jaxrs.client;

import javax.ws.rs.client.ClientRequestContext;

import io.opentracing.contrib.jaxrs.OperationNameProvider;
import io.opentracing.contrib.jaxrs.internal.URIUtils;

/**
 * @author Pavol Loffay
 */
public interface ClientOperationNameProvider extends OperationNameProvider<ClientRequestContext>{

    /**
     * As operation name provides HTTP method e.g. GET, POST..
     */
    ClientOperationNameProvider HTTP_METHOD_NAME_PROVIDER = new ClientOperationNameProvider() {
        @Override
        public String operationName(ClientRequestContext clientRequestContext) {
            return clientRequestContext.getMethod();
        }
    };

    /**
     * As operation name provides HTTP path.
     */
    ClientOperationNameProvider HTTP_PATH_NAME_PROVIDER = new ClientOperationNameProvider() {
        @Override
        public String operationName(ClientRequestContext clientRequestContext) {
            return URIUtils.path(clientRequestContext.getUri());
        }
    };

}
