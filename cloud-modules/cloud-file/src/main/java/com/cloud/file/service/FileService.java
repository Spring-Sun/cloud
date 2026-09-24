package com.cloud.file.service;

import com.cloud.common.core.exception.BusinessException;
import com.cloud.file.config.FileProperties;
import com.cloud.file.config.S3Properties;
import com.cloud.file.domain.UploadResult;
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
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 基于 S3 协议的对象存储操作。
 */
@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);
    private static final DateTimeFormatter DATE_PATH = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp");

    private final S3Client s3Client;
    private final S3Properties properties;
    private final FileProperties fileProperties;
    private final ImageConverter imageConverter;

    public FileService(S3Client s3Client, S3Properties properties,
                       FileProperties fileProperties, ImageConverter imageConverter) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.fileProperties = fileProperties;
        this.imageConverter = imageConverter;
    }

    /**
     * 上传文件。若为可转换的光栅图片且已开启 WebP，则在存储原件的同时，
     * 额外生成并存储一份 WebP 版本。
     *
     * @return 上传结果，包含原件与（可能的）WebP 对象键
     */
    public UploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String baseKey = LocalDate.now().format(DATE_PATH) + "/" + UUID.randomUUID().toString().replace("-", "");
        String objectKey = baseKey + ext;
        String contentType = file.getContentType();

        boolean convert = fileProperties.getWebp().isEnabled() && isConvertibleImage(contentType, ext);
        if (!convert) {
            try (InputStream in = file.getInputStream()) {
                PutObjectRequest request = PutObjectRequest.builder()
                        .bucket(properties.getBucket())
                        .key(objectKey)
                        .contentType(contentType)
                        .build();
                s3Client.putObject(request, RequestBody.fromInputStream(in, file.getSize()));
            } catch (IOException e) {
                log.error("upload failed for {}", objectKey, e);
                throw new BusinessException("文件上传失败: " + e.getMessage());
            }
            return UploadResult.ofOriginal(objectKey, contentType);
        }

        // 图片：读入内存，先存原件，再转存 WebP
        byte[] source;
        try {
            source = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("读取上传文件失败: " + e.getMessage());
        }
        putBytes(objectKey, contentType, source);

        String webpKey = baseKey + ".webp";
        try {
            byte[] webpBytes = imageConverter.toWebp(source, fileProperties.getWebp().getQuality());
            putBytes(webpKey, "image/webp", webpBytes);
            return UploadResult.withWebp(objectKey, webpKey, contentType);
        } catch (Exception e) {
            // 转换失败不影响原件上传，降级为仅保留原件
            log.warn("WebP 转换失败，仅保留原件, key={}, 原因={}", objectKey, e.getMessage());
            return UploadResult.ofOriginal(objectKey, contentType);
        }
    }

    private void putBytes(String key, String contentType, byte[] bytes) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(key)
                .contentType(contentType == null ? "application/octet-stream" : contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    private boolean isConvertibleImage(String contentType, String ext) {
        if (contentType != null) {
            String ct = contentType.toLowerCase(Locale.ROOT);
            if (ct.startsWith("image/")) {
                // SVG 为矢量图、webp 已是目标格式，均不再转换
                return !ct.contains("svg") && !ct.contains("webp");
            }
        }
        if (ext != null && !ext.isBlank()) {
            return IMAGE_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT));
        }
        return false;
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
