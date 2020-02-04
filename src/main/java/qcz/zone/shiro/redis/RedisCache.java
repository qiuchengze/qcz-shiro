package qcz.zone.shiro.redis;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.entity.AbstractUser;
import qcz.zone.shiro.entity.impl.ShiroUser;
import qcz.zone.shiro.manager.RedisManager;

import java.util.*;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 29
 */

public class RedisCache<K, V> implements Cache<K, V> {
    private RedisManager redisManager = null;
    private String prefix = ShiroConstant.SHIRO_REDIS_KEY$PREFIX_CACHE;
    private Long ttl = ShiroConstant.SHIRO_REDIS_EXPIRE$IN;

    public RedisCache(RedisManager redisManager) {
        this(redisManager, null, null);
    }

    public RedisCache(RedisManager redisManager, String prefix){
        this(redisManager, prefix, null);
    }

    public RedisCache(RedisManager redisManager, String prefix, Long ttl){
        if (null == redisManager)
            throw new RuntimeException("redisManager is null");

        this.redisManager = redisManager;

        if (null != prefix)
            this.prefix = prefix;

        if (null != ttl)
            this.ttl = ttl;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    @Override
    public V get(K k) throws CacheException {
        if (null == k)
            return null;

        return (V) redisManager.get(getKey(k));
    }

    @Override
    public V put(K k, V v) throws CacheException {
        if (null == k || null == v)
            return null;

        if (null != ttl && ttl > 0) {
            redisManager.set(getKey(k), v, ttl);
        } else {
            redisManager.set(getKey(k), v);
        }

        return v;
    }

    @Override
    public V remove(K k) throws CacheException {
        if(null == k)
            return null;

        V v = (V) redisManager.get(getKey(k));
        redisManager.del(getKey(k));

        return v;
    }

    @Override
    public void clear() throws CacheException {
        redisManager.flushDb();
    }

    @Override
    public int size() {
        return redisManager.dbSize().intValue();
    }

    @Override
    public Set<K> keys() {
        String k = (getPrefix() + "*");
        Set<Object> keys = redisManager.keys(k);

        Set<K> sets = new HashSet<>();

        for (Object key : keys) {
            sets.add((K)key);
        }

        return sets;
    }

    @Override
    public Collection<V> values() {
        Set<K> keys = keys();
        List<V> values = new ArrayList<>(keys.size());

        for(K k :keys){
            values.add(get(k));
        }

        return values;
    }

    private K getKey(K key){
        if (null == key)
            throw new RuntimeException("redis cache key is null");

        String k = null;
        if (key instanceof PrincipalCollection) {
            PrincipalCollection principalCollection = (PrincipalCollection) key;
            ShiroUser shiroUser = (ShiroUser) principalCollection.getPrimaryPrincipal();
            k = String.valueOf(shiroUser.getPrincipal());
        } else {
            k = key.toString();
        }

        return (K) (this.prefix + k);
    }
}
