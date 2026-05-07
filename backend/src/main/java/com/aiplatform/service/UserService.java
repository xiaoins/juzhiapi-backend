package com.aiplatform.service;

import com.aiplatform.dto.ChangePasswordRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 获取用户信息
     */
    Object getUserInfo(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 更新用户状态(管理员)
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 获取用户列表(管理员，分页)
     */
    Page<?> getUserList(int current, int size);
}
