package io.opentracing.contrib.jaxrs2.itest.cxf;

import io.opentracing.contrib.jaxrs2.itest.common.AbstractClientTest;
import java.util.concurrent.ExecutionException;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

/**
 * @author Pavol Loffay
 */
public class ApacheCXFClientITest extends AbstractClientTest {

  @Override
  protected void initServletContext(ServletContextHandler context) {
    ApacheCXFHelper.initServletContext(context);
  }

  /**
   * Does not work because client spans are not connected to parent. Apache CXF does not allow
   * to set custom ExecutorService
   */
  @Test
  @Override
  public void testAsyncMultipleRequests() throws ExecutionException, InterruptedException {
  }
}
