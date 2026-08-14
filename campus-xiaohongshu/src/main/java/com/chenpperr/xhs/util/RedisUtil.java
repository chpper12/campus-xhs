package com.chenpperr.xhs.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 *
 * 封装 StringRedisTemplate，提供常用的 String / Hash / Set / ZSet 操作。
 * 所有 key 统一用 String 类型，value 也用 String（业务层自行 JSON 序列化）。
 *
 * 命名规范：
 *   点赞 Hash  → "post:like:{postId}"   field=userId, value="1"
 *   计数缓存   → "post:like_count:{postId}"  value=数字字符串
 *   用户 Token → "user:token:{userId}"   value=token
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    // ============================== String 操作 ==============================

    /**
     * 设置 String 值
     *
     * @param key   键
     * @param value 值
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置 String 值，并指定过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时长
     * @param unit    时间单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取 String 值
     *
     * @param key 键
     * @return 值，不存在返回 null
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 自增（原子操作），key 不存在时自动初始化为 0 再 +1
     *
     * @param key   键
     * @param delta 增量（可为负数，实现自减）
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 删除指定 key
     *
     * @param key 键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除 key
     *
     * @param keys 键集合
     * @return 成功删除的数量
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 判断 key 是否存在
     *
     * @param key 键
     * @return true=存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 设置 key 的过期时间
     *
     * @param key     键
     * @param timeout 过期时长
     * @param unit    时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    // ============================== Hash 操作 ==============================

    /**
     * 向 Hash 中放入一个字段（field-value 对）
     * 如果 Hash 不存在，会自动创建
     *
     * @param key   Hash 的 key（如 "post:like:1001"）
     * @param field 字段名（如 userId "42"）
     * @param value 字段值（如 "1" 表示已点赞）
     */
    public void hSet(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 从 Hash 中获取一个字段的值
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @return 字段值，不存在返回 null
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 删除 Hash 中的一个或多个字段
     *
     * @param key    Hash 的 key
     * @param fields 要删除的字段名
     * @return 成功删除的字段数量
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断 Hash 中是否存在某个字段
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @return true=存在
     */
    public Boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 获取 Hash 中所有的字段值（用于批量刷回 MySQL）
     *
     * @param key Hash 的 key
     * @return Map<field, value>
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取 Hash 的大小（字段数量）
     *
     * @param key Hash 的 key
     * @return 字段数量
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * Hash 中某个字段的值自增（原子操作）
     * 场景：点赞计数 HINCRBY
     *
     * @param key   Hash 的 key
     * @param field 字段名
     * @param delta 增量
     * @return 自增后的值
     */
    public Long hIncrement(String key, String field, long delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    /**
     * 获取 Hash 中所有的 field 列表
     *
     * @param key Hash 的 key
     * @return field 集合
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取 Hash 中所有的 value 列表
     *
     * @param key Hash 的 key
     * @return value 列表
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    // ============================== Set 操作 ==============================

    /**
     * 向 Set 中添加元素
     *
     * @param key    Set 的 key
     * @param values 要添加的元素
     * @return 成功添加的元素数量
     */
    public Long sAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 判断 Set 中是否包含某个元素
     *
     * @param key   Set 的 key
     * @param value 要判断的元素
     * @return true=包含
     */
    public Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 从 Set 中移除元素
     *
     * @param key    Set 的 key
     * @param values 要移除的元素
     * @return 成功移除的数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 获取 Set 的大小
     *
     * @param key Set 的 key
     * @return 元素数量
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 获取 Set 中所有元素
     *
     * @param key Set 的 key
     * @return 元素集合
     */
    public Set<String> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // ============================== ZSet 操作 ==============================

    /**
     * 向 ZSet 中添加元素（带分数）
     * 场景：Feed 流用时间戳做 score
     *
     * @param key    ZSet 的 key
     * @param value  元素值
     * @param score  分数（通常用时间戳）
     * @return 是否添加成功
     */
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取 ZSet 中指定范围的元素（按 score 从大到小，即最新在前）
     *
     * @param key   ZSet 的 key
     * @param start 起始索引（0 开始）
     * @param end   结束索引（-1 表示到最后）
     * @return 元素集合
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 删除 ZSet 中的元素
     *
     * @param key    ZSet 的 key
     * @param values 要删除的元素
     * @return 成功删除的数量
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 获取 ZSet 的大小
     *
     * @param key ZSet 的 key
     * @return 元素数量
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 删除 ZSet 中指定排名范围的元素（按 score 从小到大）
     * 场景：Feed 流淘汰最旧的元素
     *
     * @param key   ZSet 的 key
     * @param start 起始索引（0 开始）
     * @param end   结束索引
     * @return 成功删除的数量
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    // ============================== SCAN 操作 ==============================

    /**
     * 基于 SCAN 游标迭代，按前缀模式匹配 key（不阻塞 Redis）
     * <p>
     * 示例：scanKeys("post:like:*") → ["post:like:1001", "post:like:1002", ...]
     *
     * @param pattern 匹配模式（支持通配符 * ? 等）
     * @return 所有匹配的 key 集合
     */
    public Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        try {
            redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Void>) connection -> {
                org.springframework.data.redis.core.ScanOptions options =
                        org.springframework.data.redis.core.ScanOptions.scanOptions()
                                .match(pattern)
                                .count(100)          // 每次迭代大约取 100 个 key
                                .build();
                try (org.springframework.data.redis.core.Cursor<byte[]> cursor =
                             connection.scan(options)) {
                    while (cursor.hasNext()) {
                        keys.add(new String(cursor.next()));
                    }
                }
                return null;
            });
        } catch (Exception e) {
            log.error("SCAN 操作失败, pattern={}", pattern, e);
        }
        return keys;
    }
}
