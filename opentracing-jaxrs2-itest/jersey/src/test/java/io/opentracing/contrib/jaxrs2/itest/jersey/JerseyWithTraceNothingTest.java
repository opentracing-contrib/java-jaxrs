package io.opentracing.contrib.jaxrs2.itest.jersey;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractServerWithTraceNothingTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Sjoerd Talsma
 */
public class JerseyWithTraceNothingTest extends AbstractServerWithTraceNothingTest {

    @Override
    public void initServletContext(ServletContextHandler context) {
        JerseyHelper.initServletContext(context);
    }

}
