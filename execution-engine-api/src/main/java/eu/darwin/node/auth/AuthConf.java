package eu.darwin.node.auth;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

import java.util.List;

@Configuration
public class AuthConf {

    @Bean
    public AuthFilter tokenFilter() {
        return new AuthFilter();
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> tenantFilterRegistration() {
        FilterRegistrationBean<AuthFilter> result = new FilterRegistrationBean<>();
        result.setFilter(this.tokenFilter());
        result.setUrlPatterns(List.of("/*"));
        result.setName("Token Store Filter");
        result.setOrder(1);
        return result;
    }

    @Bean(destroyMethod = "destroy")
    public ThreadLocalTargetSource threadLocalTenantStore() {
        ThreadLocalTargetSource result = new ThreadLocalTargetSource();
        result.setTargetBeanName("tokenStore");
        return result;
    }

    @Primary
    @Bean(name = "proxiedThreadLocalTargetSource")
    public ProxyFactoryBean proxiedThreadLocalTargetSource(ThreadLocalTargetSource threadLocalTargetSource) {
        ProxyFactoryBean result = new ProxyFactoryBean();
        result.setTargetSource(threadLocalTargetSource);
        return result;
    }

    @Bean(name = "tokenStore")
    @Scope(scopeName = "prototype")
    public UserStore tenantStore() {
        return new UserStore();
    }
}
