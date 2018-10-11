package io.opentracing.contrib.jaxrs2.discovery;

import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import javax.servlet.DispatcherType;
import javax.servlet.annotation.WebFilter;

/**
 * @author Pavol Loffay
 */
@WebFilter(urlPatterns = "/*", asyncSupported = true, dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.ASYNC})
public class DiscoverableSpanFinishingFilter extends SpanFinishingFilter {
}
