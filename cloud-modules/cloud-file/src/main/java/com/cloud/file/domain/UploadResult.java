package com.cloud.file.domain;

/**
 * 文件上传结果。
 *
 * @param originalKey 原件对象键
 * @param webpKey     WebP 版本对象键；非图片或未转换时为 {@code null}
 * @param contentType 原件的 Content-Type
 * @param webp        是否已生成 WebP 版本
 */
public record UploadResult(String originalKey, String webpKey, String contentType, boolean webp) {

    public static UploadResult ofOriginal(String originalKey, String contentType) {
        return new UploadResult(originalKey, null, contentType, false);
    }

    public static UploadResult withWebp(String originalKey, String webpKey, String contentType) {
        return new UploadResult(originalKey, webpKey, contentType, true);
    }
}
