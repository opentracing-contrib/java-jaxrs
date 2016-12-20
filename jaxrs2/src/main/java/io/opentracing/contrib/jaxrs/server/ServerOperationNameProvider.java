package io.opentracing.contrib.jaxrs.server;

import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import io.opentracing.contrib.jaxrs.OperationNameProvider;
import io.opentracing.contrib.jaxrs.internal.URIUtils;

/**
 * Span operation name provider.
 *
 * @author Pavol Loffay
 */
public interface ServerOperationNameProvider extends OperationNameProvider<ContainerRequestContext> {

    /**
     * As operation name provides HTTP method e.g. GET, POST..
     */
    ServerOperationNameProvider HTTP_METHOD_NAME_PROVIDER = new ServerOperationNameProvider() {
        @Override
        public String operationName(ContainerRequestContext containerRequestContext) {
            return containerRequestContext.getMethod();
        }
    };

    /**
     * As operation name provides HTTP path.
     */
    ServerOperationNameProvider HTTP_PATH_NAME_PROVIDER = new ServerOperationNameProvider() {
        @Override
        public String operationName(ContainerRequestContext containerRequestContext) {
            return URIUtils.path(containerRequestContext.getUriInfo().getRequestUri());
        }
    };

    /**
     * As operation name provides "wildcard" HTTP path e.g:
     *
     * resource method annotated with @Path("/foo/bar/{name: \\w+}") produces "/foo/bar/{name}"
     *
     */
    ServerOperationNameProvider HTTP_PATH_WILDCARD_NAME_PROVIDER = new ServerOperationNameProvider() {
        @Override
        public String operationName(ContainerRequestContext containerRequestContext) {
            MultivaluedMap<String, String> pathParameters = containerRequestContext.getUriInfo().getPathParameters();

            String path = URIUtils.path(containerRequestContext.getUriInfo().getRequestUri());

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
