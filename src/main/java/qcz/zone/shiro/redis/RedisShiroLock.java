package qcz.zone.shiro.redis;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import qcz.zone.redis.constant.RedisDBIndex;
import qcz.zone.redis.factory.QczRedisConnectionFactory;
import qcz.zone.redis.template.QczRedisTemplate;
import qcz.zone.shiro.config.ShiroConstant;

import java.util.concurrent.TimeUnit;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 01
 */

public class RedisShiroLock<K> {
    private QczRedisTemplate qczRedisTemplate;

    private int loginCount = ShiroConstant.SHIRO_LOGIN_COUNT;     // 允许登录重试次数
    private String loginCounterPrefix = ShiroConstant.SHIRO_REDIS_KEY$PREFIX_LOGIN$COUNTER;       // 登录失败次数计数器缓存key前缀
    private String accountLockPrefix = ShiroConstant.SHIRO_REDIS_KEY$PREFIX_ACCOUNT$IS$LOCK;      // 用户账户锁缓存key前缀
    private long lockTime = ShiroConstant.SHIRO_LOCK_ACCOUNT_TIME;        // 锁定账户时长
    private TimeUnit lockTimeUnit = ShiroConstant.SHIRO_LOCK_ACCOUNT_TIME$UNIT;   // 锁时间单位

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
    private String getCounterKey(K k) {
        if (null == k)
            throw new RuntimeException("param is null");

        return loginCounterPrefix + k;
    }

    private String getLockKey(K k) {
        if (null == k)
            throw new RuntimeException("param is null");

        return accountLockPrefix + k;
    }

    public boolean hasCounter(K k) {
        if (null == k)
            return false;

        return qczRedisTemplate.hasKey(getCounterKey(k));
    }

    public boolean hasLock(K k) {
        if (null == k)
            return false;

        return qczRedisTemplate.hasKey(getLockKey(k));
    }

    private boolean isLimitReached(Long incr) {
        if (null == incr)
            return false;

        if (incr >= loginCount)
            return true;

        return false;
    }

    // 计数器加1
    public void incrCounter(K k) {
        Long incr = qczRedisTemplate.incr(getCounterKey(k), 1);

        // 如果登录失败次数达到限定次数，则锁定账户一定时间
        if (isLimitReached(incr)) {
            setLock(k);        // 锁定
            clearCounter(k);   // 清空计数器
        }
    }

    public void clearCounter(K k) {
        if (hasCounter(k))
            qczRedisTemplate.del(getCounterKey(k));
    }

    public boolean isLock(K k) {
        if (!hasLock(k))
            return ShiroConstant.SHIRO_ACCOUNT_UNLOCK;

        Boolean lock = (Boolean) qczRedisTemplate.get(getLockKey(k));
        if (null == lock || lock)
            return ShiroConstant.SHIRO_ACCOUNT_LOCK;

        return ShiroConstant.SHIRO_ACCOUNT_UNLOCK;
    }

    public void setLock(K k) {
        if (!hasLock(k))
            qczRedisTemplate.set(getLockKey(k), ShiroConstant.SHIRO_ACCOUNT_LOCK, lockTime, lockTimeUnit);
    }

    public void clearLock(K k) {
        if (hasLock(k))
            qczRedisTemplate.del(getLockKey(k));
    }

    public void clear(K k) {
        clearLock(k);
        clearCounter(k);
    }
}
