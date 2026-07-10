package com.suda.utils;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;

/**
 * MinIO 文件上传工具类
 *
 * <p>替代原有的阿里云 OSS 工具类，使用 MinIO（S3 兼容）作为对象存储。
 * 适用于开发环境的本地 MinIO 和生产环境的 S3 兼容服务。</p>
 */
@Data
@AllArgsConstructor
@Slf4j
public class MinioUtil {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;

    /**
     * 上传文件到 MinIO
     *
     * @param bytes      文件字节数组
     * @param objectName 对象名（如 UUID.jpg）
     * @return 文件访问 URL
     */
    public String upload(byte[] bytes, String objectName) {
        MinioClient client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();

        try {
            // 确保 bucket 存在，不存在则创建
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("已创建 MinIO 存储桶: {}", bucketName);
            }

            // 上传文件
            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType("application/octet-stream")
                            .build());

        } catch (Exception e) {
            log.error("MinIO 文件上传失败: {}", objectName, e);
            throw new RuntimeException("MinIO 文件上传失败", e);
        }

        // MinIO 访问 URL 格式：{endpoint}/{bucketName}/{objectName}
        String url = String.format("%s/%s/%s", endpoint, bucketName, objectName);
        log.info("文件上传到: {}", url);
        return url;
    }

}
