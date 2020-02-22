package qcz.zone.shiro.factory.impl;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.core.io.ClassPathResource;
import qcz.zone.shiro.config.ShiroProperties;
import qcz.zone.shiro.factory.AbstractShiroFactory;
import qcz.zone.shiro.service.ShiroService;
import qcz.zone.shiro.strategy.impl.DefaultEhcacheLoginStrategy;

import javax.validation.constraints.NotNull;
import java.net.URL;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 19
 */

/**
 * Shiro构建工厂实现类（模式：EhCache）
 * 必需准备的依赖：
 * 1. ShiroService      数据源服务（实现从数据库中取用户、角色、权限、过滤配置等数据）
 * 2. EhCacheManager    EhCache缓存管理器（【 注意：传入的路径如在资源目录下，则classpath配置文件路径前需要添加'/'，如："/ehcache/ehcache-shiro.xml" 】）
 *
 * 辅助依赖：
 * 1. ShiroProperties   Shiro的一些配置项，如配置文件中未设置相关属性或未载入容器，则使用内部默认配置（ShiroConstant）
 */
public class ShiroEhCacheFactory extends AbstractShiroFactory {
    private CacheManager cacheManager = null;

    public ShiroEhCacheFactory(@NotNull ShiroService shiroService, @NotNull String ehcacheConfigurationResourceFilePath) {
        this(shiroService, ehcacheConfigurationResourceFilePath, null);
    }

    public ShiroEhCacheFactory(@NotNull ShiroService shiroService,
                               @NotNull String ehcacheConfigurationResourceFilePath,
                               ShiroProperties shiroProperties) {
        super(shiroService, shiroProperties);

        this.cacheManager = createCacheManager(ehcacheConfigurationResourceFilePath);
    }

    /** ======================================  SecurityManager  ====================================== **/
    public SecurityManager createEhCacheSecurityManager(Realm realm, SessionManager sessionManager) {
        return createDefaultWebSecurityManager(realm, sessionManager, cacheManager, null);
    }

    public SecurityManager createEhCacheSecurityManager(Realm realm,
                                                      SessionManager sessionManager,
                                                      RememberMeManager rememberMeManager) {
        return createDefaultWebSecurityManager(realm, sessionManager, cacheManager, rememberMeManager);
    }

    /** ======================================  Realm  ====================================== **/
    public Realm createEhCacheRealm() {
        CredentialsMatcher credentialsMatcher = createHashedCredentialsMatcher();
        DefaultEhcacheLoginStrategy ehCacheLoginStrategy =
                new DefaultEhcacheLoginStrategy(cacheManager);

        return createRealm(credentialsMatcher, ehCacheLoginStrategy);
    }

    public Realm createEhCacheRealm(@NotNull CredentialsMatcher credentialsMatcher) {
        DefaultEhcacheLoginStrategy ehCacheLoginStrategy = new DefaultEhcacheLoginStrategy(cacheManager);

        return createRealm(credentialsMatcher, ehCacheLoginStrategy);
    }

    /** ======================================  SessionManager  ====================================== **/
    public SessionManager createEhCacheSessionManager() {
        SessionDAO memorySessionDAO = createMemorySessionDAO();
        return createDefaultWebSessionManager(memorySessionDAO);
    }

    /** ======================================  CacheManager  ====================================== **/
    /**
     * 创建Cache缓存管理器
     * 【 可用于Shiro之外的需要使用EhCache缓存的业务使用 】
     * 【 注意：配置文件放置在资源目录下，传入的classpath配置文件路径前需要添加'/'，如："/ehcache/ehcache-shiro.xml" 】
     * @return
     */
    private CacheManager createCacheManager(String ehcacheConfigurationResourceFilePath) {
        // ClassLoader前如果不加getClass().，则打包成jar被其它引用时无法获取到资源目录
        // 建议采用getClass().getResource() 或 getClass().getClassLoader().getResource()
//            URL url = ClassLoader.getSystemResource(ehcacheConfigurationResourceFilePath);
        URL url = getClass().getResource(ehcacheConfigurationResourceFilePath);
        net.sf.ehcache.CacheManager ehCacheManager = net.sf.ehcache.CacheManager.create(url);
        ehCacheManager.setName("ShiroEhCacheManager");

        EhCacheManager cacheManager = new EhCacheManager();
        cacheManager.setCacheManager(ehCacheManager);
        return cacheManager;
    }

    /**
     * Cache缓存管理器
     * 【配合MemorySessionDAO，便于单机环境使用】
     * @return
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    /** ======================================  SessionDAO  ====================================== **/
    /**
     * 会话 内存SessionDAO
     * 【配合EhCache缓存管理器，便于单机环境使用】
     * @return
     */
    public SessionDAO createMemorySessionDAO() {
        return new MemorySessionDAO();
    }
}
