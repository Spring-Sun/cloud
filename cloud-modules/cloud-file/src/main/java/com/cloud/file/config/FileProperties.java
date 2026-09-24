package com.cloud.file.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件服务配置属性（前缀 {@code cloud.file}）。
 */
@ConfigurationProperties(prefix = "cloud.file")
@Getter
public class FileProperties {

    private final Webp webp = new Webp();

    /**
     * WebP 转码相关配置。
     */
    @Data
    public static class Webp {

        /** 是否开启图片上传后转存 WebP。 */
        private boolean enabled = true;

        /** 有损压缩质量，取值 0.0 ~ 1.0，越大画质越好、体积越大。 */
        private float quality = 0.8f;
    }
}
