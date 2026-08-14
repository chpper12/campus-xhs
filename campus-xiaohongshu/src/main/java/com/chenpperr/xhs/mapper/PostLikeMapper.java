package com.chenpperr.xhs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenpperr.xhs.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点赞记录 Mapper 接口
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
}
