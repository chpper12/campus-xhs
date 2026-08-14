package com.chenpperr.xhs.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应封装类
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页数据列表
     */
    private List<T> list;

    /**
     * 构造方法
     */
    public PageResult() {}

    /**
     * 构造方法
     */
    public PageResult(Long total, List<T> list) {
        this.total = total;
        this.list = list;
    }

    /**
     * 静态工厂方法
     */
    public static <T> PageResult<T> of(Long total, List<T> list) {
        return new PageResult<>(total, list);
    }
}
