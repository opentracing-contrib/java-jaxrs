package io.opentracing.contrib.jaxrs2.itest.resteasy;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractClassOperationNameTest;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * @author Pavol Loffay
 */
public class RestEasyClassOperationNameITest extends AbstractClassOperationNameTest {

  @Override
  protected void initServletContext(ServletContextHandler context) {
    RestEasyHelper.initServletContext(context);
  }
}
