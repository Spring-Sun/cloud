package com.cloud.file.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

/**
 * 图片格式转换器：将常见光栅图片（PNG/JPEG/GIF/BMP 等）编码为 WebP。
 *
 * <p>底层基于 Java ImageIO，WebP 编码能力由 sejda {@code webp-imageio} 插件提供
 * （内置跨平台原生库，启动时自动加载）。</p>
 */
@Component
public class ImageConverter {

    private static final Logger log = LoggerFactory.getLogger(ImageConverter.class);
    private static final String WEBP_FORMAT = "webp";

    /**
     * 确保 ImageIO 从（可能位于 fat jar 嵌套目录的）classpath 中发现并注册插件 SPI。
     */
    @PostConstruct
    public void init() {
        ImageIO.scanForPlugins();
    }

    /**
     * 将源图片字节编码为 WebP 字节。
     *
     * @param source  源图片二进制内容
     * @param quality 有损压缩质量（0.0 ~ 1.0）
     * @return WebP 编码后的字节数组
     * @throws IOException 当源内容无法解析或未找到 WebP 编码器时抛出
     */
    public byte[] toWebp(byte[] source, float quality) throws IOException {
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(source));
        if (decoded == null) {
            throw new IOException("无法解析图片内容，可能不是受支持的光栅图片格式");
        }
        BufferedImage image = normalize(decoded);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(WEBP_FORMAT);
        if (!writers.hasNext()) {
            throw new IOException("未找到 WebP 编码器，请确认 imageio-webp 依赖已引入");
        }
        ImageWriter writer = writers.next();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out)) {
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                String[] types = param.getCompressionTypes();
                if (param.getCompressionType() == null && types != null && types.length > 0) {
                    param.setCompressionType(types[0]);
                }
                param.setCompressionQuality(clamp(quality));
            }
            writer.setOutput(ios);
            writer.write(null, new IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }
        log.debug("WebP 编码完成: 源 {} 字节 -> webp {} 字节", source.length, out.size());
        return out.toByteArray();
    }

    /**
     * 将图片统一为 TYPE_INT_ARGB，避免部分编码器不支持的像素类型，同时保留透明通道。
     */
    private BufferedImage normalize(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
            return src;
        }
        BufferedImage image = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.drawImage(src, 0, 0, null);
        } finally {
            g.dispose();
        }
        return image;
    }

    private float clamp(float quality) {
        if (quality <= 0f) {
            return 0.01f;
        }
        return Math.min(quality, 1f);
    }
}
