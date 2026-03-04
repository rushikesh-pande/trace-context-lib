package com.orderprocessing.trace;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot AutoConfiguration for trace-context-lib.
 *
 * <p>Automatically registered via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 *
 * <p>Registers {@link TraceFilter} as a highest-priority servlet filter
 * when {@code trace.enabled=true} (the default).
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "trace", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TraceProperties.class)
public class TraceAutoConfig {

    @Bean
    public FilterRegistrationBean<TraceFilter> traceFilterRegistration(TraceProperties props) {
        TraceFilter filter = new TraceFilter(props.getServiceName(), props.getServicePrefix());
        FilterRegistrationBean<TraceFilter> reg = new FilterRegistrationBean<>(filter);
        reg.addUrlPatterns("/*");
        reg.setOrder(Integer.MIN_VALUE);   // highest priority
        reg.setName("traceFilter");
        return reg;
    }
}
