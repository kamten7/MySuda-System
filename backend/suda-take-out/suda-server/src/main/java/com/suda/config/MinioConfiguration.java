package com.suda.config;

import com.suda.properties.MinioProperties;
import com.suda.utils.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 对象存储配置
 *
 * <p>创建 {@link MinioUtil} Bean，用于文件上传。
 * 替代原有的阿里云 OSS 配置。</p>
 */
@Configuration
@Slf4j
public class MinioConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MinioUtil minioUtil(MinioProperties properties) {
        log.info("初始化 MinIO 上传工具: endpoint={}, bucket={}",
                properties.getEndpoint(), properties.getBucketName());
        return new MinioUtil(
                properties.getEndpoint(),
                properties.getAccessKey(),
                properties.getSecretKey(),
                properties.getBucketName());
    }

}
