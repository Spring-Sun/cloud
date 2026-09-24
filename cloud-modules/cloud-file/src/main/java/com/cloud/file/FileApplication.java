package com.cloud.file;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.cloud.file.config.FileProperties;
import com.cloud.file.config.S3Properties;

/**
 * 文件服务入口（S3 协议存储）。
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties({S3Properties.class, FileProperties.class})
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }
}
