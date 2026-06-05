package com.example.ticket.user.repository;

import com.example.ticket.user.domain.UserDO;

import java.util.Optional;

/**
 * 用户仓储接口。
 * 用于抽象用户持久化访问，隔离服务层与具体存储实现。
 */
public interface UserRepository {

    /**
     * 按用户名查询用户。
     *
     * @param username 用户名
     * @return 查询结果
     */
    Optional<UserDO> findByUsername(String username);

    /**
     * 按用户主键查询用户。
     *
     * @param userId 用户主键
     * @return 查询结果
     */
    Optional<UserDO> findById(Long userId);

    /**
     * 保存用户数据。
     *
     * @param user 用户持久化对象
     * @return 保存后的用户对象
     */
    UserDO save(UserDO user);
}
