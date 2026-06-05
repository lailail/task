package com.example.ticket.user.repository;

import com.example.ticket.user.domain.UserDO;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户内存仓储实现。
 * 这是 Phase 2 的过渡存储层，只用于稳定接口和测试结构，后续会替换为数据库实现。
 */
@Repository
@Profile("memory")
public class InMemoryUserRepository implements UserRepository {
    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final Map<String, UserDO> usersByUsername = new ConcurrentHashMap<>();

    /**
     * 按用户名从内存仓储中查询用户。
     *
     * @param username 用户名
     * @return 查询结果
     */
    @Override
    public Optional<UserDO> findByUsername(String username) {
        return Optional.ofNullable(usersByUsername.get(username));
    }

    /**
     * 按用户主键从内存仓储中查询用户。
     *
     * @param userId 用户主键
     * @return 查询结果
     */
    @Override
    public Optional<UserDO> findById(Long userId) {
        return usersByUsername.values().stream()
                .filter(user -> userId.equals(user.getUserId()))
                .findFirst();
    }

    /**
     * 保存用户到内存仓储。
     *
     * @param user 用户持久化对象
     * @return 保存后的用户对象
     */
    @Override
    public UserDO save(UserDO user) {
        if (user.getUserId() == null) {
            // 当前内存实现自行分配用户主键，后续切换到数据库时由正式持久化层接管。
            user.setUserId(idGenerator.getAndIncrement());
        }
        usersByUsername.put(user.getUsername(), user);
        return user;
    }

    /**
     * 测试辅助方法，用于在集成测试之间清空临时内存数据。
     */
    public void clear() {
        usersByUsername.clear();
        idGenerator.set(1L);
    }
}
