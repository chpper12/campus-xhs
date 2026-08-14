package com.chenpperr.xhs.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chenpperr.xhs.config.AiConfig;
import com.chenpperr.xhs.exception.BusinessException;
import com.chenpperr.xhs.common.ResultCode;
import com.chenpperr.xhs.service.AiService;
import com.chenpperr.xhs.vo.AiPolishVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI润色服务实现类
 *
 * 调用兼容OpenAI格式的大模型API进行文本润色和标签提取
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final AiConfig aiConfig;

    /**
     * 标签提取的正则模式：匹配 #xxx 格式的标签
     */
    private static final Pattern TAG_PATTERN = Pattern.compile("#([^#\\s，。！？,!?]+)");

    @Override
    public AiPolishVO polishAndExtractTags(String content) {
        log.info("开始AI润色，原始内容长度：{}", content.length());

        // 检查API Key是否配置
        if (StrUtil.isBlank(aiConfig.getApiKey()) || "your-api-key-here".equals(aiConfig.getApiKey())) {
            log.warn("AI API Key未配置，使用本地简单润色");
            return localPolish(content);
        }

        try {
            // 调用大模型API
            return callAiApi(content);
        } catch (Exception e) {
            log.error("调用AI API失败，降级为本地润色：{}", e.getMessage());
            return localPolish(content);
        }
    }

    /**
     * 调用大模型API进行润色
     *
     * @param content 原始文本
     * @return 润色结果
     */
    private AiPolishVO callAiApi(String content) {
        // 构建请求体
        JSONObject requestBody = buildRequestBody(content);

        // 发送HTTP请求
        HttpResponse response = HttpRequest.post(aiConfig.getApiUrl())
                .header(Header.CONTENT_TYPE.getValue(), "application/json")
                .header(Header.AUTHORIZATION.getValue(), "Bearer " + aiConfig.getApiKey())
                .body(JSONUtil.toJsonStr(requestBody))
                .timeout(30000) // 30秒超时
                .execute();

        // 检查响应状态
        if (response.getStatus() != 200) {
            log.error("AI API返回错误状态码：{}, 响应：{}", response.getStatus(), response.body());
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "AI服务调用失败");
        }

        // 解析响应
        return parseResponse(response.body());
    }

    /**
     * 构建API请求体
     *
     * @param content 原始文本
     * @return 请求体JSON对象
     */
    private JSONObject buildRequestBody(String content) {
        JSONObject requestBody = new JSONObject();

        // 模型名称
        requestBody.set("model", aiConfig.getModel());

        // 消息列表
        JSONArray messages = new JSONArray();

        // System提示词
        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "你是一位专业的校园社交媒体文案编辑。你的任务是：\n" +
                "1. 润色用户输入的文本，使其更生动、更有感染力，适合在校园社交平台分享\n" +
                "2. 从内容中提取2-5个合适的标签（格式：#标签名）\n" +
                "3. 保持原文的核心意思不变，不要过度修饰\n\n" +
                "请按以下JSON格式返回：\n" +
                "{\"polishedContent\": \"润色后的文本\", \"tags\": [\"#标签1\", \"#标签2\"]}");
        messages.add(systemMessage);

        // User提示词
        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "请润色以下文本并提取标签：\n\n" + content);
        messages.add(userMessage);

        requestBody.set("messages", messages);

        // 其他参数
        requestBody.set("max_tokens", aiConfig.getMaxTokens());
        requestBody.set("temperature", aiConfig.getTemperature());

        return requestBody;
    }

    /**
     * 解析API响应
     *
     * @param responseBody 响应体字符串
     * @return 润色结果
     */
    private AiPolishVO parseResponse(String responseBody) {
        try {
            JSONObject responseJson = JSONUtil.parseObj(responseBody);

            // 获取AI返回的内容（OpenAI格式）
            String aiContent = responseJson
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

            log.info("AI返回原始内容：{}", aiContent);

            // 尝试解析JSON格式的返回
            return parseAiContent(aiContent);

        } catch (Exception e) {
            log.error("解析AI响应失败：{}", e.getMessage());
            // 降级处理：直接使用返回内容作为润色结果
            return AiPolishVO.builder()
                    .polishedContent(responseBody)
                    .tags(extractTags(responseBody))
                    .build();
        }
    }

    /**
     * 解析AI返回的内容
     *
     * @param aiContent AI返回的内容
     * @return 润色结果
     */
    private AiPolishVO parseAiContent(String aiContent) {
        try {
            // 尝试解析JSON格式
            JSONObject json = JSONUtil.parseObj(aiContent);

            String polishedContent = json.getStr("polishedContent");
            JSONArray tagsArray = json.getJSONArray("tags");

            List<String> tags = new ArrayList<>();
            if (tagsArray != null) {
                for (int i = 0; i < tagsArray.size(); i++) {
                    tags.add(tagsArray.getStr(i));
                }
            }

            return AiPolishVO.builder()
                    .polishedContent(polishedContent)
                    .tags(tags)
                    .build();

        } catch (Exception e) {
            // 如果不是JSON格式，尝试手动解析
            log.warn("AI返回非JSON格式，尝试手动解析");

            String polishedContent = aiContent;
            List<String> tags = extractTags(aiContent);

            // 移除内容中的标签部分
            for (String tag : tags) {
                polishedContent = polishedContent.replace(tag, "").trim();
            }

            return AiPolishVO.builder()
                    .polishedContent(polishedContent)
                    .tags(tags)
                    .build();
        }
    }

    /**
     * 本地简单润色（降级方案）
     *
     * @param content 原始文本
     * @return 润色结果
     */
    private AiPolishVO localPolish(String content) {
        // 提取标签
        List<String> tags = extractTags(content);

        // 如果没有标签，生成默认标签
        if (tags.isEmpty()) {
            tags = generateDefaultTags(content);
        }

        // 简单润色
        String polishedContent = simplePolish(content);

        return AiPolishVO.builder()
                .polishedContent(polishedContent)
                .tags(tags)
                .build();
    }

    /**
     * 提取文本中的标签
     *
     * @param content 原始文本
     * @return 标签列表
     */
    private List<String> extractTags(String content) {
        List<String> tags = new ArrayList<>();
        Matcher matcher = TAG_PATTERN.matcher(content);

        while (matcher.find()) {
            String tag = matcher.group(0); // 包含#号
            if (!tags.contains(tag)) {
                tags.add(tag);
            }
        }

        return tags;
    }

    /**
     * 根据内容生成默认标签
     *
     * @param content 原始文本
     * @return 默认标签列表
     */
    private List<String> generateDefaultTags(String content) {
        List<String> tags = new ArrayList<>();

        if (content.contains("食堂") || content.contains("美食") || content.contains("吃") || content.contains("饭")) {
            tags.add("#美食");
        }
        if (content.contains("图书馆") || content.contains("学习") || content.contains("自习") || content.contains("考试")) {
            tags.add("#学习");
        }
        if (content.contains("操场") || content.contains("运动") || content.contains("跑步") || content.contains("健身")) {
            tags.add("#运动");
        }
        if (content.contains("穿搭") || content.contains("衣服") || content.contains("时尚")) {
            tags.add("#穿搭");
        }
        if (content.contains("游戏") || content.contains("开黑")) {
            tags.add("#游戏");
        }
        if (content.contains("宿舍") || content.contains("室友")) {
            tags.add("#宿舍");
        }

        // 添加通用标签
        tags.add("#校园生活");

        return tags;
    }

    /**
     * 简单润色文本
     *
     * @param content 原始文本
     * @return 润色后的文本
     */
    private String simplePolish(String content) {
        if (StrUtil.isBlank(content)) {
            return content;
        }

        // 如果内容太短，适当扩展
        if (content.length() < 20) {
            return content + "，分享给大家！";
        }

        return content;
    }
}
