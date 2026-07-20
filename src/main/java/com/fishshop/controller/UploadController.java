package com.fishshop.controller;

import com.fishshop.common.ApiResponse;
import com.fishshop.common.BusinessException;
import com.fishshop.common.ResultCode;
import com.fishshop.common.UserRole;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * 本地文件上传接口。
 *
 * <p>头像保存到 /uploads/images/touxiang，商品图片保存到 /uploads/images/shangpin，数据库只保存返回的访问地址。</p>
 */
@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;
    private static final String AVATAR_UPLOAD_PATH = "/uploads/images/touxiang";
    private static final String PRODUCT_UPLOAD_PATH = "/uploads/images/shangpin";

    /**
     * 上传用户头像，返回前端可直接展示的图片地址。
     */
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                            HttpServletRequest request) throws IOException {
        validateImage(file, "请选择头像图片", "头像图片不能超过 5MB");
        return ApiResponse.success(saveImage(file, request, AVATAR_UPLOAD_PATH, "avatar"));
    }

    /**
     * 上传商品图片，返回商品表 image_url 字段可保存的图片地址。
     */
    @PostMapping("/product")
    public ApiResponse<String> uploadProductImage(@RequestParam("file") MultipartFile file,
                                                  HttpServletRequest request) throws IOException {
        ensureAdmin(request);
        validateImage(file, "请选择商品图片", "商品图片不能超过 5MB");
        return ApiResponse.success(saveImage(file, request, PRODUCT_UPLOAD_PATH, "product"));
    }

    /**
     * 按图片用途保存到不同目录，并生成浏览器可直接访问的 URL。
     */
    private String saveImage(MultipartFile file,
                             HttpServletRequest request,
                             String uploadPath,
                             String filenamePrefix) throws IOException {
        String uploadRoot = request.getServletContext().getRealPath(uploadPath);
        if (uploadRoot == null) {
            throw new BusinessException("当前部署环境不支持本地图片保存");
        }

        Path uploadDir = Paths.get(uploadRoot);
        Files.createDirectories(uploadDir);

        String filename = filenamePrefix
                + "_"
                + UUID.randomUUID().toString().replace("-", "")
                + resolveExtension(file.getOriginalFilename());
        Path target = uploadDir.resolve(filename);

        // 使用随机文件名保存，避免用户上传的原始文件名带来覆盖或路径风险。
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String imageUrl = request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort()
                + request.getContextPath()
                + uploadPath
                + "/"
                + filename;
        return imageUrl;
    }

    /**
     * 只允许图片类型和合理大小，防止上传非图片文件。
     */
    private void validateImage(MultipartFile file, String emptyMessage, String sizeMessage) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(emptyMessage);
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException(sizeMessage);
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException("只能上传图片文件");
        }
    }

    /**
     * 商品图片属于后台维护能力，后端也要校验管理员角色，不能只依赖前端页面隐藏。
     */
    private void ensureAdmin(HttpServletRequest request) {
        Object currentUserRole = request.getAttribute("currentUserRole");
        if (!(currentUserRole instanceof Integer)
                || ((Integer) currentUserRole) != UserRole.ADMIN.getCode()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null) {
            return ".jpg";
        }
        String lowerName = originalFilename.toLowerCase();
        int dotIndex = lowerName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == lowerName.length() - 1) {
            return ".jpg";
        }
        String extension = lowerName.substring(dotIndex);
        if (".jpg".equals(extension) || ".jpeg".equals(extension) || ".png".equals(extension)
                || ".gif".equals(extension) || ".webp".equals(extension)) {
            return extension;
        }
        return ".jpg";
    }
}
