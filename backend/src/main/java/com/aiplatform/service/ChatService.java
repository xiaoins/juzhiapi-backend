package com.aiplatform.service;

import com.aiplatform.dto.ChatSendRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aiplatform.vo.MessageVO;
import com.aiplatform.vo.SessionVO;

/**
 * 聊天服务
 */
public interface ChatService {

    /**
     * 创建会话
     *
     * @return 会话信息
     */
    SessionVO createSession(Long userId, String modelName, String title);

    /**
     * 获取用户的会话列表(分页)
     */
    Page<SessionVO> getSessionList(Long userId, int current, int size);

    /**
     * 获取会话详情
     */
    SessionVO getSessionDetail(Long userId, Long sessionId);

    /**
     * 获取会话消息列表
     */
    Page<MessageVO> getMessages(Long userId, Long sessionId, int current, int size);

    /**
     * 发送消息 (流式)
     *
     * @return SSE 流式响应
     */
    org.springframework.web.servlet.mvc.method.annotation.SseEmitter sendMessage(Long userId, ChatSendRequest request);

    /**
     * 删除会话
     */
    void deleteSession(Long userId, Long sessionId);
}
