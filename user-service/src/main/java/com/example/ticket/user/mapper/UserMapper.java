package com.example.ticket.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ticket.user.domain.UserDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper。
 * 用于承接 `ticket_user` 表的基础 CRUD 操作，避免服务层直接拼接 SQL。
 */
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {
}
