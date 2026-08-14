package com.chenpperr.xhs.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.service.OssService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 本地文件上传服务实现类（MVP阶段，不依赖OSS）
 *
 * 文件保存在本地目录，通过静态资源映射访问
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "upload.type", havingValue = "local", matchIfMissing = true)
public class LocalUploadServiceImpl implements OssService {

    /**
     * 上传文件保存路径（使用绝对路径）
     */
    @Value("${upload.local.path:./uploads}")
    private String uploadPath;

    /**
     * 访问URL前缀
     */
    @Value("${upload.local.url-prefix:http://localhost:8081/uploads}")
    private String urlPrefix;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 上传图片到本地目录
     *
     * 流程：
     *   1. 校验文件是否为空
     *   2. 校验文件类型（仅支持 jpg/png/gif/webp）
     *   3. 生成唯一的文件名（按日期分目录 + UUID）
     *   4. 创建目录并保存文件
     *   5. 返回访问URL
     *
     * @param file 图片文件
     * @return 图片访问URL
     */
    @Override
    public String uploadImage(MultipartFile file) {
        // 第 1 步：校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }

        // 第 2 步：校验文件类型
        String originalFilename = file.getOriginalFilename();
        String extension = FileUtil.extName(originalFilename);
        if (!isAllowedImageType(extension)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "不支持的图片格式，仅支持 jpg/png/gif/webp");
        }

        // 第 3 步：生成唯一的文件名：images/2026/07/11/uuid.jpg
        String datePath = LocalDate.now().format(DATE_FORMATTER);
        String relativePath = "images/" + datePath + "/" + IdUtil.fastSimpleUUID() + "." + extension;

        // 第 4 步：拼接完整保存路径（转换为绝对路径）
        String fullPath = new File(uploadPath + "/" + relativePath).getAbsolutePath();

        try {
            // 第 5 步：创建目录
            FileUtil.mkdir(FileUtil.getParent(fullPath, 1));
            // 第 6 步：保存文件
            file.transferTo(new File(fullPath));

            // 第 7 步：返回访问URL
            String url = urlPrefix + "/" + relativePath;
            log.info("文件上传成功：{}", url);

            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件上传失败");
        }
    }

    /**
     * 校验图片类型是否允许
     *
     * @param extension 文件扩展名（如 "jpg"、"png"）
     * @return true=允许，false=不允许
     */
    private boolean isAllowedImageType(String extension) {
        if (extension == null) {
            return false;
        }
        String lowerExtension = extension.toLowerCase();
        return "jpg".equals(lowerExtension)
                || "jpeg".equals(lowerExtension)
                || "png".equals(lowerExtension)
                || "gif".equals(lowerExtension)
                || "webp".equals(lowerExtension);
    }
}
