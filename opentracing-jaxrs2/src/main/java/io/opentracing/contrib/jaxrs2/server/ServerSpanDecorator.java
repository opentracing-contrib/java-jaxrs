package io.opentracing.contrib.jaxrs2.server;

import io.opentracing.BaseSpan;
import io.opentracing.contrib.jaxrs2.internal.URIUtils;
import io.opentracing.tag.Tags;
import java.util.List;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;

/**
 * @author Pavol Loffay
 */
public interface ServerSpanDecorator {

    /**
     * Decorate span by incoming object.
     *
     * @param requestContext
     * @param span
     */
    void decorateRequest(ContainerRequestContext requestContext, BaseSpan<?> span);

    /**
     * Decorate spans by outgoing object.
     *
     * @param responseContext
     * @param span
     */
    void decorateResponse(ContainerResponseContext responseContext, BaseSpan<?> span);

    /**
     * Adds standard tags: {@link io.opentracing.tag.Tags#SPAN_KIND},
     * {@link io.opentracing.tag.Tags#HTTP_METHOD}, {@link io.opentracing.tag.Tags#HTTP_URL} and
     * {@link io.opentracing.tag.Tags#HTTP_STATUS}
     */
    ServerSpanDecorator STANDARD_TAGS = new ServerSpanDecorator() {
        @Override
        public void decorateRequest(ContainerRequestContext requestContext, BaseSpan<?> span) {
            Tags.HTTP_METHOD.set(span, requestContext.getMethod());

            String url = URIUtils.url(requestContext.getUriInfo().getAbsolutePath());
            if (url != null) {
                Tags.HTTP_URL.set(span, url);
            }
        }

        @Override
        public void decorateResponse(ContainerResponseContext responseContext, BaseSpan<?> span) {
            Tags.HTTP_STATUS.set(span, responseContext.getStatus());
        }
    };

    /**
     * As operation name provides "wildcard" HTTP path e.g:
     *
     * resource method annotated with @Path("/foo/bar/{name: \\w+}") produces "/foo/bar/{name}"
     *
     */
    ServerSpanDecorator HTTP_WILDCARD_PATH_OPERATION_NAME = new ServerSpanDecorator() {
        @Override
        public void decorateRequest(ContainerRequestContext requestContext, BaseSpan<?> span) {
            MultivaluedMap<String, String> pathParameters = requestContext.getUriInfo().getPathParameters();

            String path = URIUtils.path(requestContext.getUriInfo().getRequestUri());

            for (Map.Entry<String, List<String>> entry: pathParameters.entrySet()) {
                final String originalPathFragment = String.format("{%s}", entry.getKey());

                for (String currentPathFragment: entry.getValue()) {
                    path = path.replace(currentPathFragment, originalPathFragment);
                }
            }

            span.setOperationName(path);
        }

        @Override
        public void decorateResponse(ContainerResponseContext responseContext, BaseSpan<?> span) {
        }
    };

}
