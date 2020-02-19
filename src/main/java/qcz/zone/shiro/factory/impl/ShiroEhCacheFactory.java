package qcz.zone.shiro.factory.impl;

import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import qcz.zone.shiro.config.ShiroProperties;
import qcz.zone.shiro.factory.AbstractShiroFactory;
import qcz.zone.shiro.service.ShiroService;
import qcz.zone.shiro.strategy.impl.DefaultEhcacheLoginStrategy;

import javax.validation.constraints.NotNull;

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
 *
 * 辅助依赖：
 * 1. ShiroProperties   Shiro的一些配置项，如配置文件中未设置相关属性或未载入容器，则使用内部默认配置（ShiroConstant）
 */
public class ShiroEhCacheFactory extends AbstractShiroFactory {
    private CacheManager ehCacheManager = null;

    public ShiroEhCacheFactory(@NotNull ShiroService shiroService, @NotNull CacheManager ehCacheManager) {
        this(shiroService, ehCacheManager, null);
    }

    public ShiroEhCacheFactory(@NotNull ShiroService shiroService,
                               @NotNull CacheManager ehCacheManager,
                               ShiroProperties shiroProperties) {
        super(shiroService, shiroProperties);

        this.ehCacheManager = ehCacheManager;
    }

    /** ======================================  SecurityManager  ====================================== **/
    public SecurityManager createEhCacheSecurityManager(Realm realm, SessionManager sessionManager) {
        return createDefaultWebSecurityManager(realm, sessionManager, ehCacheManager, null);
    }

    public SecurityManager createEhCacheSecurityManager(Realm realm,
                                                      SessionManager sessionManager,
                                                      RememberMeManager rememberMeManager) {
        return createDefaultWebSecurityManager(realm, sessionManager, ehCacheManager, rememberMeManager);
    }

    /** ======================================  Realm  ====================================== **/
    public Realm createEhCacheRealm() {
        CredentialsMatcher credentialsMatcher = createHashedCredentialsMatcher();
        DefaultEhcacheLoginStrategy ehCacheLoginStrategy = new DefaultEhcacheLoginStrategy(ehCacheManager);

        return createRealm(credentialsMatcher, ehCacheLoginStrategy);
    }

    public Realm createEhCacheRealm(@NotNull CredentialsMatcher credentialsMatcher) {
        DefaultEhcacheLoginStrategy ehCacheLoginStrategy = new DefaultEhcacheLoginStrategy(ehCacheManager);

        return createRealm(credentialsMatcher, ehCacheLoginStrategy);
    }

    /** ======================================  SessionManager  ====================================== **/
    public SessionManager createEhCacheSessionManager() {
        SessionDAO memorySessionDAO = createMemorySessionDAO();
        return createDefaultWebSessionManager(memorySessionDAO);
    }

    /** ======================================  CacheManager  ====================================== **/
//    /**
//     * 本地内存缓存
//     * shiro自带的MemoryConstrainedCacheManager作缓存
//     * 只能用于本机，集群时无法使用
//     * @return
//     */
//    public CacheManager createMemoryCcacheManager() {
//        MemoryConstrainedCacheManager cacheManager=new MemoryConstrainedCacheManager(); //使用内存缓存
//
//        return cacheManager;
//    }

    /**
     * EhCache缓存管理器工厂Bean
     * 【 此工厂Bean必需使用动态代理形式才能正常读取xml配置文件并创建cacheManager，
     * 因此需使用注解（@Configuration/@Bean、@Component等），交由Spring容器管理 】
     * @return
     */
    public static EhCacheManagerFactoryBean createEhCacheManagerFactoryBean(@NotNull  String xmlResourcePath) {
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        // xml默认设为resources目录下: ehcache.xml    (classpath:ehcache.xml) 无需classpath:
        ehCacheManagerFactoryBean.setConfigLocation(new ClassPathResource(xmlResourcePath));
        ehCacheManagerFactoryBean.setCacheManagerName("EhCacheManager");
        ehCacheManagerFactoryBean.setShared(true);

        return ehCacheManagerFactoryBean;
    }

    public static EhCacheManagerFactoryBean createEhCacheManagerFactoryBean(@NotNull  String xmlResourcePath,
                                                                     String cacheManagerName,
                                                                     Boolean isShared) {
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        ehCacheManagerFactoryBean.setConfigLocation(new ClassPathResource(xmlResourcePath));

        if (!StringUtils.isEmpty(cacheManagerName))
            ehCacheManagerFactoryBean.setCacheManagerName(cacheManagerName);

        if (null != isShared)
            ehCacheManagerFactoryBean.setShared(isShared);

        return ehCacheManagerFactoryBean;
    }

    /**
     * EhCache缓存管理器
     * 【配合MemorySessionDAO，便于单机环境使用】
     * @return
     */
    public static CacheManager createEhCacheManager(EhCacheManagerFactoryBean ehCacheManagerFactoryBean) {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager(ehCacheManagerFactoryBean.getObject());

        return ehCacheManager;
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
