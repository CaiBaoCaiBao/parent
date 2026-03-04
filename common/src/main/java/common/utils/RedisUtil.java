package common.utils;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@NoArgsConstructor
@Component
public class RedisUtil {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置键值对（不带过期时间）
     *
     * @param key   键
     * @param value 值
     */
    @Description("设置键值 - 不带时效")
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置键值对（带过期时间）
     *
     * @param key   键
     * @param value 值
     * @param time  过期时间（秒）
     */
    @Description("设置键值 - 携带带时效")
    public void set(String key, Object value, long time) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 根据键获取值
     *
     * @param key 键
     * @return 值，如果键为null则返回null
     */
    @Description("获取键值")
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除单个键
     *
     * @param key 键
     * @return 删除成功返回true，失败返回false
     */
    @Description("删除")
    public Boolean del(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 批量删除键
     *
     * @param keys 键集合
     * @return 删除的键数量
     */
    public Long del(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 批量删除键（可变参数）
     *
     * @param keys 键数组
     * @return 删除的键数量，如果键数组为空则返回0
     */
    public Long del(String... keys) {
        if (keys == null || keys.length == 0) {
            return 0L;
        }
        return redisTemplate.delete(List.of(keys));
    }

    /**
     * 设置键的过期时间
     *
     * @param key  键
     * @param time 过期时间（秒），必须大于0
     * @return 设置成功返回true，失败返回false
     */
    public Boolean expire(String key, long time) {
        try {
            if (time > 0) {
                return redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取键的剩余过期时间
     *
     * @param key 键
     * @return 剩余过期时间（秒），-2表示键不存在，-1表示键没有设置过期时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断键是否存在
     *
     * @param key 键
     * @return 存在返回true，不存在或发生异常返回false
     */
    public Boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据模式匹配获取所有键
     *
     * @param pattern 匹配模式，如 "user:*"
     * @return 匹配的键集合
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 向Hash中设置字段值（不带过期时间）
     *
     * @param key   Hash键
     * @param item  字段名
     * @param value 字段值
     */
    public void hset(String key, String item, Object value) {
        redisTemplate.opsForHash().put(key, item, value);
    }

    /**
     * 向Hash中设置字段值（带过期时间）
     *
     * @param key   Hash键
     * @param item  字段名
     * @param value 字段值
     * @param time  过期时间（秒）
     */
    public void hset(String key, String item, Object value, long time) {
        redisTemplate.opsForHash().put(key, item, value);
        if (time > 0) {
            expire(key, time);
        }
    }

    /**
     * 从Hash中获取字段值
     *
     * @param key  Hash键
     * @param item 字段名
     * @return 字段值
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取Hash中的所有字段和值
     *
     * @param key Hash键
     * @return 字段和值的Map
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 删除Hash中的字段
     *
     * @param key  Hash键
     * @param item 要删除的字段名（可变参数）
     * @return 删除成功返回true，失败返回false
     */
    public Boolean hdel(String key, Object... item) {
        return redisTemplate.opsForHash().delete(key, item) > 0;
    }

    /**
     * 判断Hash中是否存在指定字段
     *
     * @param key  Hash键
     * @param item 字段名
     * @return 存在返回true，不存在返回false
     */
    public Boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * Hash字段值递增
     *
     * @param key  Hash键
     * @param item 字段名
     * @param by   递增量
     * @return 递增后的值
     */
    public Long hincr(String key, String item, long by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * Hash字段值递增（浮点数）
     *
     * @param key  Hash键
     * @param item 字段名
     * @param by   递增量
     * @return 递增后的值
     */
    public Double hincrByDouble(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * 获取Hash中的所有字段名
     *
     * @param key Hash键
     * @return 字段名集合
     */
    public Set<Object> hkeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取Hash中的所有值
     *
     * @param key Hash键
     * @return 值列表
     */
    public List<Object> hvals(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 获取Hash的大小（字段数量）
     *
     * @param key Hash键
     * @return 字段数量
     */
    public Long hsize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 向Set集合中添加元素
     *
     * @param key    Set键
     * @param values 要添加的元素（可变参数）
     * @return 添加成功的元素数量
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 获取Set集合中的所有元素
     *
     * @param key Set键
     * @return 元素集合
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 判断元素是否在Set集合中
     *
     * @param key   Set键
     * @param value 元素值
     * @return 存在返回true，不存在返回false
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取Set集合的大小
     *
     * @param key Set键
     * @return 元素数量
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 从Set集合中移除元素
     *
     * @param key    Set键
     * @param values 要移除的元素（可变参数）
     * @return 移除成功的元素数量
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 向List列表右侧推入元素（不带过期时间）
     *
     * @param key   List键
     * @param value 元素值
     * @return 推入后列表的大小
     */
    public Long lPush(String key, Object value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 向List列表右侧推入元素（带过期时间）
     *
     * @param key   List键
     * @param value 元素值
     * @param time  过期时间（秒）
     * @return 推入后列表的大小
     */
    public Long lPush(String key, Object value, long time) {
        Long index = redisTemplate.opsForList().rightPush(key, value);
        if (time > 0) {
            expire(key, time);
        }
        return index;
    }

    /**
     * 获取List列表指定范围内的元素
     *
     * @param key   List键
     * @param start 开始位置（0表示第一个元素）
     * @param end   结束位置（-1表示最后一个元素）
     * @return 元素列表
     */
    public List<Object> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取List列表的大小
     *
     * @param key List键
     * @return 元素数量
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 获取List列表中指定索引位置的元素
     *
     * @param key   List键
     * @param index 索引位置（0表示第一个元素，-1表示最后一个元素）
     * @return 元素值
     */
    public Object lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 从List列表中移除指定值的元素
     *
     * @param key   List键
     * @param count 移除数量（大于0从表头开始，小于0从表尾开始，等于0移除所有）
     * @param value 要移除的元素值
     * @return 移除的元素数量
     */
    public Long lRemove(String key, long count, Object value) {
        return redisTemplate.opsForList().remove(key, count, value);
    }

    /**
     * 向有序集合中添加元素
     *
     * @param key   有序集合键
     * @param value 元素值
     * @param score 分数
     * @return 添加成功返回true，失败返回false
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 获取有序集合指定范围内的元素（按分数从小到大排序）
     *
     * @param key   有序集合键
     * @param start 开始位置（0表示第一个元素）
     * @param end   结束位置（-1表示最后一个元素）
     * @return 元素集合
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 从有序集合中移除元素
     *
     * @param key    有序集合键
     * @param values 要移除的元素（可变参数）
     * @return 移除的元素数量
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 获取有序集合中元素的分数
     *
     * @param key   有序集合键
     * @param value 元素值
     * @return 分数值，元素不存在返回null
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 获取有序集合的大小
     *
     * @param key 有序集合键
     * @return 元素数量
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }
}