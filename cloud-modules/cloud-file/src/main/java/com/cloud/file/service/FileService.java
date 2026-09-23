package com.cloud.file.service;

import com.cloud.common.core.exception.BusinessException;
import com.cloud.file.config.S3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 基于 S3 协议的对象存储操作。
 */
@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Client s3Client;
    private final S3Properties properties;

    public FileService(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    /**
     * 上传文件，返回生成的对象键（object key）。
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String objectKey = LocalDate.now().format(DATE_PATH) + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        try (InputStream in = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
        } catch (IOException e) {
            log.error("upload failed for {}", objectKey, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
        return objectKey;
    }

    /**
     * 下载对象，返回其内容与内容类型。
     */
    public FileContent download(String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request)) {
            byte[] bytes = response.readAllBytes();
            String contentType = response.response().contentType();
            return new FileContent(bytes, contentType == null ? "application/octet-stream" : contentType);
        } catch (IOException e) {
            log.error("download failed for {}", objectKey, e);
            throw new BusinessException("文件下载失败: " + e.getMessage());
        }
    }

    /**
     * 删除对象。
     */
    public void delete(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(objectKey)
                .build();
        s3Client.deleteObject(request);
    }

    /**
     * 下载内容的简单持有类。
     */
    public record FileContent(byte[] data, String contentType) {
    }
}
