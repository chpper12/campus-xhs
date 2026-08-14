package com.chenpperr.xhs.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文件上传响应视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 图片访问URL
     */
    private String url;

    /**
     * 文件名
     */
    private String filename;
}
