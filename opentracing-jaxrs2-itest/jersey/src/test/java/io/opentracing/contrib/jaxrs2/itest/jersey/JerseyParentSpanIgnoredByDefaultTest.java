package io.opentracing.contrib.jaxrs2.itest.jersey;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractParentSpanResolutionTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

public class JerseyParentSpanIgnoredByDefaultTest extends AbstractParentSpanResolutionTest{
    @Override
    protected boolean shouldUseParentSpan() {
        return true;
    }

    @Override
    protected void initServletContext(ServletContextHandler context) {
        super.initServletContext(context);
        JerseyHelper.initServletContext(context);
    }
}
