package com.chenpperr.xhs.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户资料视图对象（个人主页用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 发布笔记数
     */
    private Long postCount;

    /**
     * 关注数
     */
    private Long followingCount;

    /**
     * 粉丝数
     */
    private Long followerCount;

    /**
     * 当前登录用户是否已关注此人（查看他人主页时有效，查看自己主页时为 null）
     */
    private Boolean isFollowed;
}
