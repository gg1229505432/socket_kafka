package com.yenanren.socket_kafka.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


/**
 * 聊天消息 , 用户和系统
 * 也就是聊天页面
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Messages {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String uuid = UUID.randomUUID().toString();
    /**
     * 聊天室成员ID
     */
    private int chatroomId;
    /**
     * 聊天室名称
     */
    @TableField(exist = false)
    private String chatroomName;
    /**
     * 聊天室定义的 id
     */
    @TableField(exist = false)
    private Integer chatroomsDefId;
    /**
     * 成员ID
     */
    private int userId;

    /**
     * 用户名
     */
    @TableField(exist = false)
    private String username;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 是否是自己发送 , 是1 , 否0
     */
    private int isSelf;
    /**
     * 发送时间
     */
    private Long sendDate;
    /**
     * 消息状态
     */
    private String status;
    /**
     * 消息类型
     */
    private String type;
    /**
     * 消息附件
     */
    private String attachment;
    /**
     * 回复消息ID
     */
    private int replyMessageId;
    /**
     * 消息标签
     */
    private String messageLabel;

    public Messages(Integer chatroomsDefId, String content, int isSelf) {
        this.chatroomsDefId = chatroomsDefId;
        this.content = content;
        this.isSelf = isSelf;
    }
}