package com.chenpperr.xhs.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.config.OssConfig;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * OSS 上传服务实现类
 * 仅当 upload.type=oss 时生效
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "upload.type", havingValue = "oss")
@RequiredArgsConstructor
public class OssServiceImpl implements OssService {

    private final OSS ossClient;
    private final OssConfig ossConfig;

    /**
     * 日期格式化器
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 上传图片到阿里云 OSS
     *
     * 流程：
     *   1. 校验文件是否为空
     *   2. 校验文件类型（仅支持 jpg/png/gif/webp）
     *   3. 生成唯一的文件名（按日期分目录 + UUID）
     *   4. 上传到 OSS
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
        String newFileName = "images/" + datePath + "/" + IdUtil.fastSimpleUUID() + "." + extension;

        try {
            // 第 4 步：上传文件到 OSS
            ossClient.putObject(ossConfig.getBucketName(), newFileName, file.getInputStream());

            // 第 5 步：拼接访问URL并返回
            String url = ossConfig.getUrlPrefix() + "/" + newFileName;
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
