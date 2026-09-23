package com.cloud.common.security.config;

import com.cloud.common.security.interceptor.UserContextInterceptor;
import com.cloud.common.security.utils.JwtUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 面向 Servlet 服务的安全自动配置：暴露 {@link JwtUtils}，
 * 并注册基于请求头的 {@link UserContextInterceptor}。
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(SecurityProperties properties) {
        return new JwtUtils(properties);
    }

    @Bean
    public WebMvcConfigurer userContextWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new UserContextInterceptor()).addPathPatterns("/**");
            }
        };
    }
}
