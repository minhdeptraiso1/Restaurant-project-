package com.hoabanrestaurant.backendchat.config;

import com.hoabanrestaurant.backendchat.security.InternalAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final InternalAuthFilter filter;

    @Bean
    public FilterRegistrationBean<InternalAuthFilter> registerFilter() {
        FilterRegistrationBean<InternalAuthFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.addUrlPatterns("/api/ai/*");
        bean.setOrder(0);
        return bean;
    }
}
