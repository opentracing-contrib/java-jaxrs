package io.opentracing.contrib.jaxrs2.itest.resteasy;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerWithTraceNothingTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Sjoerd Talsma
 */
public class RestEasyWithTraceNothingTest extends AbstractServerWithTraceNothingTest {

    @Override
    protected void initServletContext(ServletContextHandler context) {
        RestEasyHelper.initServletContext(context);
    }
}
