package com.chenpperr.xhs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenpperr.xhs.entity.Post;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记 Mapper 接口
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
}