package com.chenpperr.xhs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * AI润色请求参数
 */
@Data
public class AiPolishDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户输入的原始文本
     */
    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容长度不能超过1000字")
    private String content;
}
