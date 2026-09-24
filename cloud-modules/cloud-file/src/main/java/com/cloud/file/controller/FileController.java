package com.cloud.file.controller;

import com.cloud.common.core.domain.R;
import com.cloud.file.domain.UploadResult;
import com.cloud.file.service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口。通过网关以 {@code /file/**} 访问。
 */
@RestController
@RequestMapping("/oss")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public R<UploadResult> upload(@RequestParam("file") MultipartFile file) {
        return R.ok("上传成功", fileService.upload(file));
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("key") String key) {
        FileService.FileContent content = fileService.download(key);
        String filename = key.contains("/") ? key.substring(key.lastIndexOf('/') + 1) : key;
        ByteArrayResource resource = new ByteArrayResource(content.data());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(content.contentType()))
                .contentLength(content.data().length)
                .body(resource);
    }

    @DeleteMapping("/delete")
    public R<Void> delete(@RequestParam("key") String key) {
        fileService.delete(key);
        return R.ok();
    }
}
