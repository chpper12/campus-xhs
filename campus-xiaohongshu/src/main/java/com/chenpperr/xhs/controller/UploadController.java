package com.chenpperr.xhs.controller;

import com.chenpperr.xhs.common.Result;
import com.chenpperr.xhs.service.OssService;
import com.chenpperr.xhs.vo.UploadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UploadController {

    private final OssService ossService;

    /**
     * 上传单张图片
     *
     * @param file 图片文件
     * @return 上传结果（包含URL和文件名）
     */
    @PostMapping("/upload/image")
    public Result<UploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("开始上传图片，文件名：{}", file.getOriginalFilename());

        // 调用OSS服务上传图片
        String url = ossService.uploadImage(file);

        // 构建响应对象
        UploadVO uploadVO = UploadVO.builder()
                .url(url)
                .filename(file.getOriginalFilename())
                .build();

        return Result.success("上传成功", uploadVO);
    }
}
