package io.opentracing.contrib.jaxrs2.itest.resteasy;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractParentSpanResolutionTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class RestEasyParentSpanIgnoredByDefaultTest extends AbstractParentSpanResolutionTest {

  @Override
  protected boolean shouldUseParentSpan() {
    return true;
  }

  protected void initServletContext(ServletContextHandler context) {
    super.initServletContext(context);
    RestEasyHelper.initServletContext(context);
  }

}
