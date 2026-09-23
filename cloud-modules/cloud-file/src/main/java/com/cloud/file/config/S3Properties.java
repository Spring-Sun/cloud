package com.cloud.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * S3 兼容存储配置属性（前缀 {@code cloud.s3}）。适用于 AWS S3、
 * MinIO、Ceph RGW 以及其他 S3 协议对象存储。
 */
@Data
@ConfigurationProperties(prefix = "cloud.s3")
public class S3Properties {

    /** 服务端点，例如 <a href="https://s3.amazonaws.com">...</a>，或 MinIO 的 <a href="http://127.0.0.1:9000">...</a>。 */
    private String endpoint = "http://127.0.0.1:9000";

    /** AWS 区域。 */
    private String region = "us-east-1";

    /** 访问密钥（access key）。 */
    private String accessKey = "minioadmin";

    /** 私密密钥（secret key）。 */
    private String secretKey = "minioadmin";

    /** 默认存储桶名称。 */
    private String bucket = "cloud";

    /** 是否使用路径风格访问（MinIO 及大多数自建存储需要）。 */
    private boolean pathStyleAccess = true;

}
