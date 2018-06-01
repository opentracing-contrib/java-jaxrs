package io.opentracing.contrib.jaxrs2.server;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.internal.CastUtils;
import io.opentracing.contrib.jaxrs2.internal.SpanWrapper;
import io.opentracing.tag.Tags;
import io.opentracing.util.GlobalTracer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter which finishes span after server processing. It is required to be registered.
 *
 * @author Pavol Loffay
 */
@WebFilter(urlPatterns = "/*", asyncSupported = true)
public class SpanFinishingFilter implements Filter {

  private final Tracer tracer;

  public SpanFinishingFilter() {
    this(GlobalTracer.get());
  }

  public SpanFinishingFilter(Tracer tracer){
    this.tracer = tracer;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    HttpServletResponse httpResponse = (HttpServletResponse)response;
    HttpServletRequest httpRequest = (HttpServletRequest)request;

    try {
      chain.doFilter(request, response);
    } catch (Exception ex) {
      SpanWrapper spanWrapper = getSpanWrapper(httpRequest);
      if (spanWrapper != null) {
        Tags.HTTP_STATUS.set(spanWrapper.get(), httpResponse.getStatus());
        addExceptionLogs(spanWrapper.get(), ex);
      }
      throw ex;
    } finally {
      SpanWrapper spanWrapper = getSpanWrapper(httpRequest);
      if (spanWrapper != null) {
        spanWrapper.getScope().close();
        if (request.isAsyncStarted()) {
          request.getAsyncContext().addListener(new SpanFinisher(spanWrapper), request, response);
        } else {
          spanWrapper.finish();
        }
      }
    }
  }

  private SpanWrapper getSpanWrapper(HttpServletRequest request) {
    return CastUtils.cast(request.getAttribute(SpanWrapper.PROPERTY_NAME), SpanWrapper.class);
  }

  @Override
  public void destroy() {
  }

  static class SpanFinisher implements AsyncListener {
    private SpanWrapper spanWrapper;
    SpanFinisher(SpanWrapper spanWrapper) {
      this.spanWrapper = spanWrapper;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
      spanWrapper.finish();
    }
    @Override
    public void onTimeout(AsyncEvent event) throws IOException {
    }
    @Override
    public void onError(AsyncEvent event) throws IOException {
      // this handler is called when exception is thrown in async handler
      // note that exception logs are added in filter not here
    }
    @Override
    public void onStartAsync(AsyncEvent event) throws IOException {
    }
  }

  private static void addExceptionLogs(Span span, Throwable throwable) {
    Tags.ERROR.set(span, true);
    Map<String, Object> errorLogs = new HashMap<>(2);
    errorLogs.put("event", Tags.ERROR.getKey());
    errorLogs.put("error.object", throwable);
    span.log(errorLogs);
  }
}
