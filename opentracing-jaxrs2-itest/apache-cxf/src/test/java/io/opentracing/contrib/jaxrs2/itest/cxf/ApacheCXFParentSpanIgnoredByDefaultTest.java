package io.opentracing.contrib.jaxrs2.itest.cxf;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractParentSpanResolutionTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class ApacheCXFParentSpanIgnoredByDefaultTest extends AbstractParentSpanResolutionTest {

  @Override
  protected boolean shouldUseParentSpan() {
    return false;
  }

  @Override
  protected void initServletContext(ServletContextHandler context) {
    super.initServletContext(context);
    ApacheCXFHelper.initServletContext(context);
  }
}
