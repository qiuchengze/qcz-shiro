package qcz.zone.shiro.factory.impl;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.config.ShiroProperties;
import qcz.zone.shiro.factory.AbstractShiroFactory;
import qcz.zone.shiro.lock.impl.RedisShiroLock;
import qcz.zone.shiro.manager.RedisCacheManager;
import qcz.zone.shiro.manager.RedisManager;
import qcz.zone.shiro.redis.RedisSessionDAO;
import qcz.zone.shiro.service.ShiroService;
import qcz.zone.shiro.strategy.impl.DefaultRedisLoginStrategy;

import javax.validation.constraints.NotNull;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 19
 */

/**
 * Shiro构建工厂实现类（模式：Redis）
 * 必需准备的依赖：
 * 1. ShiroService      数据源服务（实现从数据库中取用户、角色、权限、过滤配置等数据）
 *
 * 辅助依赖：
 * 1. ShiroProperties   Shiro的一些配置项，如配置文件中未设置相关属性或未载入容器，则使用内部默认配置（ShiroConstant）
 */
public class ShiroRedisFactory extends AbstractShiroFactory {
    private RedisProperties redisProperties = null;
    private RedisManager redisManager = null;
    private CacheManager redisCacheManager = null;

    public ShiroRedisFactory(@NotNull ShiroService shiroService, @NotNull RedisProperties redisProperties) {
        this(shiroService, redisProperties, null);
    }

    public ShiroRedisFactory(@NotNull ShiroService shiroService,
                             @NotNull RedisProperties redisProperties,
                             ShiroProperties shiroProperties) {
        super(shiroService, shiroProperties);

        this.redisManager = createRedisManager(redisProperties);
        this.redisCacheManager = createRedisCacheManager(redisManager);
    }

    /** ======================================  SecurityManager  ====================================== **/
    public SecurityManager createRedisSecurityManager(Realm realm, SessionManager sessionManager) {
        return createDefaultWebSecurityManager(realm, sessionManager, redisCacheManager, null);
    }

    public SecurityManager createRedisSecurityManager(Realm realm,
                                                SessionManager sessionManager,
                                                RememberMeManager rememberMeManager) {
        return createDefaultWebSecurityManager(realm, sessionManager, redisCacheManager, rememberMeManager);
    }

    /** ======================================  Realm  ====================================== **/
    public Realm createRedisRealm() {
        CredentialsMatcher credentialsMatcher = createHashedCredentialsMatcher();
        RedisShiroLock redisShiroLock = new RedisShiroLock(redisProperties);
        DefaultRedisLoginStrategy redisLoginStrategy = new DefaultRedisLoginStrategy(redisShiroLock);

        return createRealm(credentialsMatcher, redisLoginStrategy);
    }

    public Realm createRedisRealm(@NotNull CredentialsMatcher credentialsMatcher) {
        RedisShiroLock redisShiroLock = new RedisShiroLock(redisProperties);
        DefaultRedisLoginStrategy redisLoginStrategy = new DefaultRedisLoginStrategy(redisShiroLock);

        return createRealm(credentialsMatcher, redisLoginStrategy);
    }

    /** ======================================  SessionManager  ====================================== **/
    public SessionManager createRedisSessionManager() {
        SessionDAO redisSessionDAO = createRedisSessionDAO(redisManager);
        return createDefaultWebSessionManager(redisSessionDAO);
    }

    /** ======================================  CacheManager  ====================================== **/
    /**
     * CacheManager 缓存管理器
     * 【 使用自定义的Redis管理器 】
     * @param redisManager
     * @return
     */
    public CacheManager createRedisCacheManager(@NotNull RedisManager redisManager) {
        if (null == redisManager)
            return null;

        return new RedisCacheManager(redisManager,
                ShiroConstant.SHIRO_REDIS_KEY$PREFIX_CACHE,
                ShiroConstant.SHIRO_CACHE_EXPIRE$IN);
    }

    /**
     * 自定义实现的Redis管理器
     * @return
     */
    public RedisManager createRedisManager(@NotNull RedisProperties redisProperties) {
        return new RedisManager(redisProperties);
    }

    /** ======================================  SessionDAO  ====================================== **/
    /**
     * 会话RedisDAO
     * 【 使用自定义的RedisManager 】
     * @param redisManager
     * @return
     */
    public SessionDAO createRedisSessionDAO(RedisManager redisManager) {
        if (null == redisManager)
            return null;

        return new RedisSessionDAO(
                redisManager,
                ShiroConstant.SHIRO_REDIS_KEY$PREFIX_SESSION,
                ShiroConstant.SHIRO_CACHE_EXPIRE$IN);
    }
}
