package qcz.zone.shiro.manager;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.util.StringUtils;
import qcz.zone.redis.constant.RedisDBIndex;
import qcz.zone.redis.factory.QczRedisConnectionFactory;
import qcz.zone.redis.template.QczRedisTemplate;

import java.util.Set;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 28
 */

public class RedisManager<K, V> {
    private QczRedisTemplate redisTemplate = null;
    private RedisDBIndex redisDBIndex = RedisDBIndex.DB_INDEX_0;
    private RedisConnectionFactory redisConnectionFactory = null;

    public RedisManager(QczRedisTemplate qczRedisTemplate) {
        if (null == qczRedisTemplate)
            throw new RuntimeException("qczRedisTemplate is null");

        this.redisTemplate = qczRedisTemplate;
        this.redisDBIndex = RedisDBIndex.getEnum(qczRedisTemplate.getIndex());
        this.redisConnectionFactory = qczRedisTemplate.getConnectionFactory();

        this.redisTemplate.selectObjectStreamSerializable();
    }

    public RedisManager(RedisProperties redisProperties) {
        this(redisProperties, RedisDBIndex.DB_INDEX_0, null);
    }

    public RedisManager(RedisProperties redisProperties, RedisDBIndex redisDBIndex) {
        this(redisProperties, redisDBIndex, null);
    }

    public RedisManager(RedisProperties redisProperties, RedisDBIndex redisDBIndex, RedisConnectionFactory redisConnectionFactory) {
        if (null == redisProperties)
            throw new RuntimeException("redisProperties is null");

        if (null != redisDBIndex)
            this.redisDBIndex = redisDBIndex;

        if (null != redisConnectionFactory) {
            this.redisConnectionFactory = redisConnectionFactory;
        } else {
            this.redisConnectionFactory = defaultRedisConnectionFactory(redisProperties, redisDBIndex.getValue());
        }

        this.redisTemplate = new QczRedisTemplate(this.redisDBIndex, this.redisConnectionFactory);
        this.redisTemplate.selectObjectStreamSerializable();
    }

    private RedisConnectionFactory defaultRedisConnectionFactory(RedisProperties redisProperties, int dbIndex) {
        return QczRedisConnectionFactory.createLettuceConnectionFactory(dbIndex, redisProperties);
    }

    public V get(K k) {
        if (StringUtils.isEmpty(k))
            return null;

        if (!redisTemplate.hasKey(k))
            return null;

        return (V) redisTemplate.get(k);
    }

    public void set(K k, V v) {
        set(k, v, 0);
    }

    public void set(K k, V v, long expireTime) {
        if (StringUtils.isEmpty(k) || null == v)
            return;

        if (expireTime > 0) {
            redisTemplate.set(k, v, expireTime);
        } else {
            redisTemplate.set(k, v);
        }
    }

    public void del(K k) {
        if (StringUtils.isEmpty(k))
            return;

        if (!redisTemplate.hasKey(k))
            return;

        redisTemplate.del(k);
    }

    public Set<K> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public Set<V> vals(String pattern) {
        Set<K> keys = keys(pattern);

        return (Set<V>) redisTemplate.vals(keys);
    }

    public Long dbSize() {
        return redisTemplate.dbSize();
    }

    public void flushDb() {
        redisTemplate.flushDb();
    }
}
