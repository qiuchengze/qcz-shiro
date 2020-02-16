package qcz.zone.shiro.lock.impl;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import qcz.zone.shiro.lock.ShiroLock;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 16
 */

public class EhCacheShiroLock<K> extends ShiroLock<K> {
    private static final String CACHE_NAME = "";

    @Cacheable(value = CACHE_NAME, key = "#getCounterKey(#k)", condition = "null != #k", unless = "null == #result")
    public Integer getCounter(K k) {
        return 0;
    }

    @CachePut(value = CACHE_NAME, key = "#getCounterKey(#k)", condition = "null != #k", unless = "null == #result")
    public Integer addCounter(K k, Integer v) {
        if (null == v)
            v = 0;

        return v;
    }

    @CacheEvict(value = CACHE_NAME, key = "#getCounterKey(#k)", condition = "null != #k")
    public void delCounter(K k) {
    }

    @Cacheable(value = CACHE_NAME, key = "#getLockKey(#k)", condition = "null != #k", unless = "null == #result")
    public Integer getLock(K k) {
        return 0;
    }

    @Cacheable(value = CACHE_NAME, key = "#getLockKey(#k)", condition = "null != #k", unless = "null == #result")
    public Integer addLock(K k) {
        return 1;
    }

    @CacheEvict(value = CACHE_NAME, key = "#getLockKey(#k)", condition = "null != #k")
    public void delLock(K k) {
    }

    @Override
    public boolean hasCounter(K k) {
        if (null == getCounter(k) || 0 <= getCounter(k))
            return false;

        return true;
    }

    @Override
    public boolean hasLock(K k) {
        if (null == getLock(k) || 0 <= getLock(k))
            return false;

        return true;
    }

    @Override
    public void incrCounter(K k) {
        Integer counter = getCounter(k);
        if (null == counter)
            counter = 0;

        counter ++;

        addCounter(k, counter);

        // 如果登录失败次数达到限定次数，则锁定账户一定时间
        if (isLimitReached(counter)) {
            setLock(k);        // 锁定
            delCounter(k);   // 清空计数器
        }
    }

    @Override
    public boolean isLock(K k) {
        Integer lock = getLock(k);
        if (null == lock || 0 <= lock)
            return false;

        return true;
    }

    @Override
    public void setLock(K k) {
        addLock(k);
    }
}
