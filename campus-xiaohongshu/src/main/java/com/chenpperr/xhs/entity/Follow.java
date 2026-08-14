package com.chenpperr.xhs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关注关系实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("follow")
public class Follow implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关注者ID（谁关注的）
     */
    private Long userId;

    /**
     * 被关注者ID（关注了谁）
     */
    private Long followUserId;

    /**
     * 关注时间
     */
    private LocalDateTime createTime;
}
