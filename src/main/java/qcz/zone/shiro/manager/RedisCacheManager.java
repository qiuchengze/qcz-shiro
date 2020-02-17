package qcz.zone.shiro.manager;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.util.CollectionUtils;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.redis.RedisCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 29
 */

public class RedisCacheManager implements CacheManager {

    private RedisManager RedisManager = null;
    private final Map<String, Cache> mapCaches = new ConcurrentHashMap<String, Cache>();
    private String prefix = ShiroConstant.SHIRO_REDIS_KEY$PREFIX_CACHE;
    private Long ttl = ShiroConstant.SHIRO_CACHE_EXPIRE$IN;

    public RedisCacheManager(RedisManager redisManager) {
        this(redisManager, null,null);
    }

    public RedisCacheManager(RedisManager redisManager, String prefix) {
        this(redisManager, prefix, null);
    }

    public RedisCacheManager(RedisManager redisManager, String prefix, Long ttl) {
        if (null == redisManager)
            throw new RuntimeException("RedisManager is null");

        this.RedisManager = redisManager;

        if (null != prefix)
            this.prefix = prefix;

        if (null != ttl)
            this.ttl = ttl;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String key) throws CacheException {
        Cache cache = null;

        if (!CollectionUtils.isEmpty(mapCaches) && mapCaches.containsKey(key))
            cache = mapCaches.get(key);

        if (null == cache) {
            cache = new RedisCache(RedisManager, prefix + key, ttl);

            mapCaches.put(key, cache);
        }

        return cache;
    }

    public RedisManager getRedisManager() {
        return RedisManager;
    }

    public void setRedisManager(RedisManager RedisManager) {
        this.RedisManager = RedisManager;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }
}
