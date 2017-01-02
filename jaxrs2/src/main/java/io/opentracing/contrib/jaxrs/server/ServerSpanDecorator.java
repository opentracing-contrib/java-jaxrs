package io.opentracing.contrib.jaxrs.server;

import java.util.List;
import java.util.Map;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.MultivaluedMap;

import io.opentracing.Span;
import io.opentracing.contrib.jaxrs.SpanDecorator;
import io.opentracing.contrib.jaxrs.internal.URIUtils;
import io.opentracing.tag.Tags;

/**
 * @author Pavol Loffay
 */
public interface ServerSpanDecorator extends SpanDecorator<ContainerRequestContext, ContainerResponseContext> {

    /**
     * Adds standard tags: {@link io.opentracing.tag.Tags#SPAN_KIND}, {@link io.opentracing.tag.Tags#HTTP_METHOD} and
     * {@link io.opentracing.tag.Tags#HTTP_URL}
     */
    ServerSpanDecorator STANDARD_TAGS = new ServerSpanDecorator() {
        @Override
        public void decorateRequest(ContainerRequestContext requestContext, Span span) {
            Tags.SPAN_KIND.set(span, Tags.SPAN_KIND_SERVER);
            Tags.HTTP_METHOD.set(span, requestContext.getMethod());

            String url = URIUtils.url(requestContext.getUriInfo().getAbsolutePath());
            if (url != null) {
                Tags.HTTP_URL.set(span, url);
            }
        }

        @Override
        public void decorateResponse(ContainerResponseContext responseContext, Span span) {
            Tags.HTTP_STATUS.set(span, responseContext.getStatus());
        }
    };

    /**
     * As operation name provides HTTP method e.g. GET, POST..
     */
    ServerSpanDecorator HTTP_METHOD_OPERATION_NAME = new ServerSpanDecorator() {
        @Override
        public void decorateRequest(ContainerRequestContext requestContext, Span span) {
            span.setOperationName(requestContext.getMethod());
        }

        @Override
        public void decorateResponse(ContainerResponseContext responseContext, Span span) {
        }
    };

    /**
     * As operation name provides HTTP path.
     */
    ServerSpanDecorator HTTP_PATH_OPERATION_NAME = new ServerSpanDecorator() {
        @Override
        public void decorateRequest(ContainerRequestContext requestContext, Span span) {
            span.setOperationName(URIUtils.path(requestContext.getUriInfo().getRequestUri()));
        }

        @Override
        public void decorateResponse(ContainerResponseContext responseContext, Span span) {
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
        public void decorateRequest(ContainerRequestContext requestContext, Span span) {
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
        public void decorateResponse(ContainerResponseContext responseContext, Span span) {
        }
    };

}
