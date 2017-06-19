package io.opentracing.contrib.jaxrs2.itest.jersey;

import java.util.concurrent.ExecutorService;
import org.glassfish.jersey.client.ClientAsyncExecutor;
import org.glassfish.jersey.spi.ExecutorServiceProvider;

/**
 * @author Pavol Loffay
 */
@ClientAsyncExecutor
public class DelegateExecutorServiceProvider implements ExecutorServiceProvider {

  private final ExecutorService executorService;

  public DelegateExecutorServiceProvider(ExecutorService executorService) {
    this.executorService = executorService;
  }

  @Override
  public ExecutorService getExecutorService() {
    return executorService;
  }

  @Override
  public void dispose(ExecutorService executorService) {
  }
}
