package com.chenpperr.xhs.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * AI润色响应视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiPolishVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * AI润色后的文本
     */
    private String polishedContent;

    /**
     * AI提取的标签列表
     */
    private List<String> tags;
}
