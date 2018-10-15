package io.opentracing.contrib.jaxrs2.itest.cxf;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerWithTraceNothingTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Sjoerd Talsma
 */
public class ApacheCXFWithTraceNothingTest extends AbstractServerWithTraceNothingTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        ApacheCXFHelper.initServletContext(context);
    }

}
