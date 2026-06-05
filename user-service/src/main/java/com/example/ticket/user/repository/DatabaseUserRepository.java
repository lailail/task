package com.example.ticket.user.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ticket.user.domain.UserDO;
import com.example.ticket.user.mapper.UserMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据库仓储实现。
 * 当前通过 MyBatis-Plus 访问 `ticket_user` 表，对服务层继续暴露稳定的仓储接口。
 */
@Repository
public class DatabaseUserRepository implements UserRepository {
    private final UserMapper userMapper;

    /**
     * 构造用户数据库仓储。
     *
     * @param userMapper 用户表 Mapper
     */
    public DatabaseUserRepository(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 按用户名查询用户。
     *
     * @param username 用户名
     * @return 查询结果
     */
    @Override
    public Optional<UserDO> findByUsername(String username) {
        return Optional.ofNullable(userMapper.selectOne(
                new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, username)
        ));
    }

    /**
     * 按用户主键查询用户。
     *
     * @param userId 用户主键
     * @return 查询结果
     */
    @Override
    public Optional<UserDO> findById(Long userId) {
        return Optional.ofNullable(userMapper.selectById(userId));
    }

    /**
     * 保存用户数据。
     *
     * @param user 用户持久化对象
     * @return 保存后的用户对象
     */
    @Override
    public UserDO save(UserDO user) {
        if (user.getUserId() == null) {
            userMapper.insert(user);
            return user;
        }
        userMapper.updateById(user);
        return user;
    }
}
