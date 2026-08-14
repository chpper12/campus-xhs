package com.chenpperr.xhs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 发布笔记请求参数
 */
@Data
public class PostPublishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记标题
     */
    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题长度不能超过50字")
    private String title;

    /**
     * 分区
     */
    @NotBlank(message = "分区不能为空")
    private String category;

    /**
     * 用户原始内容
     */
    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容长度不能超过1000字")
    private String content;

    /**
     * 图片URL列表（前端先上传图片拿到URL，再和笔记一起提交）
     */
    private List<String> imageUrls;
}
