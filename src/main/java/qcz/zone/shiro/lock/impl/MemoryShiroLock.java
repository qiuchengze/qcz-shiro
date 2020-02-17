package qcz.zone.shiro.lock.impl;

import qcz.zone.shiro.lock.ShiroLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 16
 */

public class MemoryShiroLock<K> extends ShiroLock<K> {
    private Map<String, Integer> mapCounter = null;
    private Map<String, Integer> mapLock = null;

    public MemoryShiroLock() {
        mapCounter = new ConcurrentHashMap<String, Integer>();
        mapLock = new ConcurrentHashMap<String, Integer>();
    }

    public void delLock(K k) {
        if (null == k)
            return;

        mapLock.remove(getLockKey(k));
    }

    @Override
    public boolean hasCounter(K k) {
        if (null == k)
            return false;

        return mapCounter.containsKey(getCounterKey(k));
    }

    @Override
    public boolean hasLock(K k) {
        if (null == k)
            return false;

        return mapLock.containsKey(getLockKey(k));
    }

    @Override
    public void incrCounter(K k) {
        if (null == k)
            return;

        Integer count = (null == mapCounter.get(getCounterKey(k)) ? 0 : mapCounter.get(getCounterKey(k)) + 1);

        // 如果登录失败次数达到限定次数，则锁定账户一定时间
        if (isLimitReached(count)) {
            setLock(k);        // 锁定
            delCounter(k);   // 清空计数器
        } else {
            mapCounter.put(getCounterKey(k), count);
        }
    }

    @Override
    public void delCounter(K k) {
        if (null == k)
            return;

        mapCounter.remove(getCounterKey(k));
    }

    @Override
    public boolean isLock(K k) {
        if (null == k)
            return false;

        if (hasLock(k)) {
            Integer lock = mapLock.get(getLockKey(k));
            if (null != lock && lock > 0)
                return true;
        }

        return false;
    }

    @Override
    public void setLock(K k) {
        if (null == k)
            return;

        mapLock.put(getLockKey(k), 1);
    }
}
