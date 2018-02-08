package io.opentracing.contrib.jaxrs2.example.spring.boot;

import io.opentracing.Tracer;
import io.opentracing.contrib.jaxrs2.server.SpanFinishingFilter;
import javax.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * @author Pavol Loffay
 */
@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public Tracer tracer() {
        return new LoggingTracer();
    }

    @Bean
    public FilterRegistrationBean spanFinishingFilter(Tracer tracer) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new SpanFinishingFilter(tracer));
        filterRegistrationBean.setAsyncSupported(true);
        filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        filterRegistrationBean.addUrlPatterns("*");
        return filterRegistrationBean;
    }
}
