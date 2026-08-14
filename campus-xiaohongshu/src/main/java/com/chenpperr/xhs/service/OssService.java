package com.chenpperr.xhs.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * OSS 上传服务接口
 */
public interface OssService {

    /**
     * 上传图片到 OSS
     *
     * @param file 图片文件
     * @return 图片访问URL
     */
    String uploadImage(MultipartFile file);
}
