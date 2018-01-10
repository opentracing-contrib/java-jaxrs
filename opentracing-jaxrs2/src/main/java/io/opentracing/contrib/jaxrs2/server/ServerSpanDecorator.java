package io.opentracing.contrib.jaxrs2.server;

import io.opentracing.BaseSpan;
import io.opentracing.contrib.jaxrs2.internal.URIUtils;
import io.opentracing.tag.Tags;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

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
}
