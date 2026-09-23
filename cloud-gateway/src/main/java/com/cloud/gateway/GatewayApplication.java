package com.cloud.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.cloud.gateway.config.GatewayAuthProperties;

/**
 * 响应式 API 网关入口。所有外部请求都经由本服务路由。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(GatewayAuthProperties.class)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
