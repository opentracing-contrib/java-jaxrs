package io.opentracing.contrib.jaxrs.server;

import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author Pavol Loffay
 */
public interface OperationNameProvider {

    String operationName(ContainerRequestContext containerRequestContext);

    OperationNameProvider HTTP_METHOD_NAME_PROVIDER = new OperationNameProvider() {
        @Override
        public String operationName(ContainerRequestContext containerRequestContext) {
            return containerRequestContext.getMethod();
        }
    };

    OperationNameProvider HTTP_PATH_NAME_PROVIDER = new OperationNameProvider() {
        @Override
        public String operationName(ContainerRequestContext containerRequestContext) {
            String path = containerRequestContext.getUriInfo().getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }

            return path;
        }
    };

    OperationNameProvider HTTP_PATH_WILDCARD_NAME_PROVIDER = new OperationNameProvider() {
        @Override
        public String operationName(ContainerRequestContext containerRequestContext) {
            MultivaluedMap<String, String> pathParameters = containerRequestContext.getUriInfo().getPathParameters();

            String path = containerRequestContext.getUriInfo().getPath();
            if (path != null && path.startsWith("/")) {
                path = path.substring(1);
            }

            for (Map.Entry<String, List<String>> entry: pathParameters.entrySet()) {
                final String originalPathFragment = String.format("{%s}", entry.getKey());

                for (String currentPathFragment: entry.getValue()) {
                    path = path.replace(currentPathFragment, originalPathFragment);
                }
            }

            return path;
        }
    };
}
