package com.chenpperr.xhs.service;

import com.chenpperr.xhs.vo.AiPolishVO;

/**
 * AI润色服务接口
 */
public interface AiService {

    /**
     * 润色文本并提取标签
     *
     * @param content 用户输入的原始文本
     * @return 润色后的文本和标签
     */
    AiPolishVO polishAndExtractTags(String content);
}
