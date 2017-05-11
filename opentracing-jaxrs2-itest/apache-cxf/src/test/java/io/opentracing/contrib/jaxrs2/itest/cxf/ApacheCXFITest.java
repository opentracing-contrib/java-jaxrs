package io.opentracing.contrib.jaxrs2.itest.cxf;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class ApacheCXFITest extends AbstractServerTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        ApacheCXFHelper.initServletContext(context);
    }
}
