package com.suda.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO 对象存储配置属性
 */
@Component
@ConfigurationProperties(prefix = "suda.minio")
@Data
public class MinioProperties {

    /** MinIO 服务地址，如 http://127.0.0.1:9010 */
    private String endpoint;

    /** 访问密钥（S3 Access Key） */
    private String accessKey;

    /** 秘密密钥（S3 Secret Key） */
    private String secretKey;

    /** 存储桶名称 */
    private String bucketName;

}
