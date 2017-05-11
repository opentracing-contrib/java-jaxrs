package io.opentracing.contrib.jaxrs2.itest.resteasy;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class RestEasyITest extends AbstractServerTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        RestEasyHelper.initServletContext(context);
    }
}
