package qcz.zone.shiro.lock.impl;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.checkerframework.checker.units.qual.C;
import qcz.zone.shiro.factory.impl.ShiroEhCacheFactory;
import qcz.zone.shiro.lock.ShiroLock;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 16
 */

public class EhCacheShiroLock<K> extends ShiroLock<K> {
    private CacheManager cacheManager = null;
    private Cache<Object, Object> ehCache = null;

    public EhCacheShiroLock(CacheManager cacheManager) {
        if (null == cacheManager)
            throw new RuntimeException("【EhCacheShiroLock】 CacheManager is null");

        this.cacheManager = cacheManager;

        ehCache = cacheManager.getCache("lockCache");
    }

    public void delLock(K k) {
        if (null == k)
            return;

        ehCache.remove(getLockKey(k));
    }

    @Override
    public boolean hasCounter(K k) {
        if (null == k)
            return false;

        return (null == ehCache.get(getCounterKey(k)) ? false : true);
    }

    @Override
    public boolean hasLock(K k) {
        if (null == k)
            return false;

        return (null == ehCache.get(getLockKey(k)) ? false : true);
    }

    @Override
    public void incrCounter(K k) {
        if (null == k)
            return;

        Integer count = (null == ehCache.get(getCounterKey(k)) ? 0 : (Integer) ehCache.get(getCounterKey(k)) + 1);

        // 如果登录失败次数达到限定次数，则锁定账户一定时间
        if (isLimitReached(count)) {
            setLock(k);        // 锁定
            delCounter(k);   // 清空计数器
        } else {
            ehCache.put(getCounterKey(k), count);
        }
    }

    @Override
    public void delCounter(K k) {
        if (null == k)
            return;

        ehCache.remove(getCounterKey(k));
    }

    @Override
    public boolean isLock(K k) {
        if (null == k)
            return false;

        if (hasLock(k)) {
            Integer lock = (Integer) ehCache.get(getLockKey(k));
            if (null != lock && lock > 0)
                return true;
        }

        return false;
    }

    @Override
    public void setLock(K k) {
        if (null == k)
            return;

        ehCache.put(getLockKey(k), 1);
    }
}
