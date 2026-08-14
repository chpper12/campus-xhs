package com.chenpperr.xhs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenpperr.xhs.entity.Follow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 关注关系 Mapper
 */
@Mapper
public interface FollowMapper extends BaseMapper<Follow> {
}
