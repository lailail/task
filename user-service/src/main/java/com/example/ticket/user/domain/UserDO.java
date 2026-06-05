package com.example.ticket.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 用户持久化对象。
 * 当前用于承载用户基础信息与加密密码字段。
 */
@TableName("ticket_user")
public class UserDO {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    private String username;
    private String password;
    @TableField("display_name")
    private String displayName;

    /**
     * 获取用户主键。
     *
     * @return 用户主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户主键。
     *
     * @param userId 用户主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名。
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取加密后的密码。
     *
     * @return 加密密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置加密后的密码。
     *
     * @param password 加密密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取展示名称。
     *
     * @return 展示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 设置展示名称。
     *
     * @param displayName 展示名称
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
