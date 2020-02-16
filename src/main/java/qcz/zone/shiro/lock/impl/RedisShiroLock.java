package qcz.zone.shiro.lock.impl;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import qcz.zone.redis.constant.RedisDBIndex;
import qcz.zone.redis.factory.QczRedisConnectionFactory;
import qcz.zone.redis.template.QczRedisTemplate;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.lock.ShiroLock;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 01
 */

public class RedisShiroLock<K> extends ShiroLock<K> {
    private QczRedisTemplate qczRedisTemplate;

    public RedisShiroLock(RedisProperties redisProperties) {
        if (null == redisProperties)
            throw new RuntimeException("redisProterties is null");

        RedisConnectionFactory redisConnectionFactory =
                QczRedisConnectionFactory.createLettuceConnectionFactory(RedisDBIndex.DB_INDEX_0.getValue(), redisProperties);

        if (null == redisConnectionFactory)
            throw new RuntimeException("redisConnectionFactory is null");

        qczRedisTemplate = new QczRedisTemplate(redisConnectionFactory);
        if (null == qczRedisTemplate)
            throw new RuntimeException("qczRedisTemplate is null");
    }

    @Override
    public boolean hasCounter(K k) {
        if (null == k)
            return false;

        return qczRedisTemplate.hasKey(getCounterKey(k));
    }

    @Override
    public boolean hasLock(K k) {
        if (null == k)
            return false;

        return qczRedisTemplate.hasKey(getLockKey(k));
    }

    @Override
    public void incrCounter(K k) {
        Integer incr = qczRedisTemplate.incr(getCounterKey(k), 1).intValue();

        // 如果登录失败次数达到限定次数，则锁定账户一定时间
        if (isLimitReached(incr)) {
            setLock(k);        // 锁定
            delCounter(k);   // 清空计数器
        }
    }

    @Override
    public void delCounter(K k) {
        if (hasCounter(k))
            qczRedisTemplate.del(getCounterKey(k));
    }

    @Override
    public boolean isLock(K k) {
        if (!hasLock(k))
            return ShiroConstant.SHIRO_ACCOUNT_UNLOCK;

        Boolean lock = (Boolean) qczRedisTemplate.get(getLockKey(k));
        if (null == lock || lock)
            return ShiroConstant.SHIRO_ACCOUNT_LOCK;

        return ShiroConstant.SHIRO_ACCOUNT_UNLOCK;
    }

    @Override
    public void setLock(K k) {
        if (!hasLock(k))
            qczRedisTemplate.set(getLockKey(k), ShiroConstant.SHIRO_ACCOUNT_LOCK, getLockTime(), getLockTimeUnit());
    }

    @Override
    public void delLock(K k) {
        if (hasLock(k))
            qczRedisTemplate.del(getLockKey(k));
    }
}
