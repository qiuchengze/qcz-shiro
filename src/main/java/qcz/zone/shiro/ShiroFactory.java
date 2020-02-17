package qcz.zone.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.MemoryConstrainedCacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.MemorySessionDAO;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.config.ShiroProperties;
import qcz.zone.shiro.entity.ShiroUrlAccessStrategy;
import qcz.zone.shiro.manager.*;
import qcz.zone.shiro.redis.RedisSessionDAO;
import qcz.zone.shiro.filter.ShiroAuthenticationFilter;
import qcz.zone.shiro.filter.ShiroAuthorizationFilter;
import qcz.zone.shiro.realm.ShiroRealm;
import qcz.zone.shiro.lock.impl.RedisShiroLock;
import qcz.zone.shiro.service.ShiroService;
import qcz.zone.shiro.strategy.AbstractLoginStrategy;
import qcz.zone.shiro.strategy.impl.DefaultEhcacheLoginStrategy;
import qcz.zone.shiro.strategy.impl.DefaultRedisLoginStrategy;

import javax.servlet.Filter;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 02
 */

public class ShiroFactory {
    private ShiroService shiroService = null;           // @Service，用于从数据库中获取用户数据
    private ShiroProperties shiroProperties = null;     // Shiro属性配置
    private RedisProperties redisProperties = null;     // Redis属性配置
    private RedisManager defaultRedisManager = null;    // 默认Redis管理器

    /**
     * Shiro工厂Bean 构造方法
     * 【 使用内部EhCache的用户登录策略方式 】
     * @param shiroService
     * @param shiroProperties
     */
    public ShiroFactory(
            ShiroService shiroService,
            ShiroProperties shiroProperties) {
        this(shiroService, shiroProperties, null);
    }

    /**
     * Shiro工厂Bean 构造方法
     * 【 使用自定义的用户登录策略方式 】
     * @param shiroService
     * @param shiroProperties
     * @param redisProperties
     */
    public ShiroFactory(ShiroService shiroService, ShiroProperties shiroProperties, RedisProperties redisProperties) {

        if (null == shiroService) {
            throw new RuntimeException("shiroService is null");
        } else {
            this.shiroService = shiroService;
        }

        if (null == shiroProperties) {
            throw new RuntimeException("shiroProperties is null");
        } else {
            this.shiroProperties = shiroProperties;
        }

        if (null != redisProperties) {
            this.redisProperties = redisProperties;
            this.defaultRedisManager = createRedisManager();
        }

        ShiroConstant.init(shiroProperties);
    }

    /** ======================================  FilterBean  ====================================== **/
    /**
     * 创建Shiro拦截器及拦截策略
     * @param securityManager
     * @returns
     */
    public ShiroFilterFactoryBean createShiroFilter(SecurityManager securityManager) {
        if (null == securityManager)
            throw new RuntimeException("securityManager is null");

        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        shiroFilterFactoryBean.setSecurityManager(securityManager);
        shiroFilterFactoryBean.setLoginUrl(ShiroConstant.SHIRO_CONFIG_LOGIN$URL);
        shiroFilterFactoryBean.setSuccessUrl(ShiroConstant.SHIRO_CONFIG_SUCCESS$URL);
        shiroFilterFactoryBean.setUnauthorizedUrl(ShiroConstant.SHIRO_CONFIG_UNAUTHORIZED$URL);

        Map<String, Filter> mapFilters = new LinkedHashMap<String, Filter>();
        mapFilters.put("perms", new ShiroAuthorizationFilter());
        mapFilters.put("authc", new ShiroAuthenticationFilter());    // 先放入认证拦截器
//        mapFilters.put("authc", new FormAuthenticationFilter());    // 表单认证过滤器
        shiroFilterFactoryBean.setFilters(mapFilters);

        List<ShiroUrlAccessStrategy> lstUrlAccessStrategy = (List<ShiroUrlAccessStrategy>) shiroService.getAllUrlAccessStrategy();

        if (!CollectionUtils.isEmpty(lstUrlAccessStrategy)) {
            Map<String, String> mapFilterChainDefinition = new LinkedHashMap<String, String>();
            for (ShiroUrlAccessStrategy uas : lstUrlAccessStrategy) {
                if (null != uas)
                    mapFilterChainDefinition.put(uas.getUrl(), uas.getFilters());
            }

            if (!CollectionUtils.isEmpty(mapFilterChainDefinition))
                shiroFilterFactoryBean.setFilterChainDefinitionMap(mapFilterChainDefinition);
        }

        return shiroFilterFactoryBean;
    }

    /** ======================================  SecurityManager  ====================================== **/
    /**
     * Web类型安全管理器
     * 【 1. 使用自定义Redis缓存管理器方式 或 不使用Redis缓存管理器（不使用传null）】
     * 【 2. 使用自定义记住我功能 或 不使用记住我功能（不使用传null） 】
     * @param realm
     * @param sessionManager
     * @param cacheManager
     * @param rememberMeManager
     * @return
     */
    public SecurityManager createDefaultWebSecurityManager(
            @NotNull Realm realm,
            @NotNull SessionManager sessionManager,
            CacheManager cacheManager,
            RememberMeManager rememberMeManager
    ) {
        return ShiroSecurityManagerBiulder.Builder
                .setRealm(realm)
                .setSessionManager(sessionManager)
                .setCacheManager(cacheManager)
                .setRememberMeManager(rememberMeManager).build();
    }

    /** ======================================  Realm  ====================================== **/
    /**
     * Shiro域（用户身份、角色、权限、认证凭证器匹配器等数据源）
     * 【 使用内部提供的凭证匹配器 】
     * @return
     */
    public Realm createRealm(AbstractLoginStrategy loginStrategy) {
        CredentialsMatcher credentialsMatcher = createHashedCredentialsMatcher();

        return createRealm(credentialsMatcher, loginStrategy);
    }

    /**
     * Shiro域（用户身份、角色、权限、认证凭证器匹配器等数据源）
     * 【 使用自定义的凭证匹配器 】
     * @param credentialsMatcher
     * @return
     */
    public Realm createRealm(@NotNull CredentialsMatcher credentialsMatcher, AbstractLoginStrategy loginStrategy) {
        if (null == shiroService)
            throw new RuntimeException("shiroService is null");

        if (null == credentialsMatcher)
            throw new RuntimeException("credentialsMatcher is null");

        ShiroRealm shiroRealm = new ShiroRealm(shiroService, loginStrategy);
        shiroRealm.setCredentialsMatcher(credentialsMatcher);

        return shiroRealm;
    }

    /**
     * 内部默认凭证匹配器
     * @return
     */
    private HashedCredentialsMatcher createHashedCredentialsMatcher() {
        return createHashedCredentialsMatcher(
                ShiroConstant.SHIRO_CONFIG_HASH$ALGORITHM$NAME,     // MD5加密
                ShiroConstant.SHIRO_CONFIG_HASH$ITERATIONS_1024);   // 加密迭代次数 1024次
    }

    /**
     * 自定义凭证匹配器
     * @param algorithmName 加密方式名
     * @param iterations    加密迭代次数
     * @return
     */
    public HashedCredentialsMatcher createHashedCredentialsMatcher(String algorithmName, Integer iterations) {
        if (StringUtils.isEmpty(algorithmName) || null == iterations || 0 == iterations)
            return null;

        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashAlgorithmName(algorithmName);  // 加密方式
        hashedCredentialsMatcher.setHashIterations(iterations);     // 加密迭代次数

        return hashedCredentialsMatcher;
    }

    /** ======================================  SessionManager  ====================================== **/
    /**
     * 无状态Web会话管理器
     * 【 如不使用SessionDAO来缓存会话，则SessionDAO传null 】
     * @param sessionDAO
     * @return
     */
    public DefaultWebSessionManager createStatelessSessionManager(SessionDAO sessionDAO) {
        return createDefaultWebSessionManager(new StatelessWebSessionManager(), sessionDAO);
    }

    /**
     * 默认Web会话管理器（默认Web类型会话管理器）
     * 【 如不使用SessionDAO来缓存会话，则SessionDAO传null 】
     * @param sessionDAO
     * @return
     */
    public DefaultWebSessionManager createDefaultWebSessionManager(SessionDAO sessionDAO) {
        DefaultWebSessionManager webSessionManager = createDefaultWebSessionManager();

        return createDefaultWebSessionManager(webSessionManager, sessionDAO);
    }

    /**
     * 自定义会话管理器
     * 【 如不使用SessionDAO来缓存会话，则SessionDAO传null 】
     * @param sessionManager
     * @param sessionDAO
     * @return
     */
    public DefaultWebSessionManager createDefaultWebSessionManager(DefaultWebSessionManager sessionManager, SessionDAO sessionDAO){
        if (null == sessionManager)
            throw new RuntimeException("sessionManager is null");

        // 注入自定义Session持久化器
        if (null != sessionDAO)
            sessionManager.setSessionDAO(sessionDAO);

        return sessionManager;
    }

    /**
     * 默认Web会话管理器
     * @return
     */
    private DefaultWebSessionManager createDefaultWebSessionManager() {
        DefaultWebSessionManager webSessionManager = new DefaultWebSessionManager();
        // 缓存超时时间
        webSessionManager.setGlobalSessionTimeout(ShiroConstant.SHIRO_CONFIG_SESSION$TIMEOUT);
        // 定时清理失效会话, 清理用户直接关闭浏览器造成的孤立会话
        // 缓存清理间隔时间
        webSessionManager.setSessionValidationInterval(ShiroConstant.SHIRO_CONFIG_SESSION$TIMEOUT);
        // 允许删除无效的会话
        webSessionManager.setDeleteInvalidSessions(true);
        // 开启周期清理
        webSessionManager.setSessionValidationSchedulerEnabled(true);

        /**
         * 是否将SessionId写入Cookie
         * true==>写入：有状态环境（如：PC浏览器Web环境），此环境如果非true，则无法记录登录状态。
         * false==>不写入：无状态环境，如：APP）
         * 【 双模式，增加业务判断进行自适应动态设置 】
         */
        webSessionManager.setSessionIdCookieEnabled(true);
//        sessionManager.setSessionIdCookie(sessionIdCookie);    // 注入cookie
        webSessionManager.setSessionIdUrlRewritingEnabled(false);  // 去掉shiro登录时url里的JSESSIONID

        return webSessionManager;
    }

    /** ======================================  RememberMeManager  ====================================== **/
    /**
     * Cookie管理器（记住我功能）
     * @return
     */
    public CookieRememberMeManager createRememberMeManager(){
        CookieRememberMeManager cookieRememberMeManager = new CookieRememberMeManager();
        cookieRememberMeManager.setCookie(rememberMeCookie());
        //rememberMe cookie加密的密钥 建议每个项目都不一样 默认AES算法 密钥长度(128 256 512 位)
        cookieRememberMeManager.setCipherKey(Base64.decode("3AvVhmFLUs0KTA3Kprsdag=="));

        return cookieRememberMeManager;
    }

    /**
     * Cookie对象
     * @return
     */
    private SimpleCookie rememberMeCookie(){
        //这个参数是cookie的名称，对应前端的checkbox的name = rememberMe
        SimpleCookie simpleCookie = new SimpleCookie("rememberMe");
        //<!-- 记住我cookie生效时间30天 ,单位秒;-->
        simpleCookie.setMaxAge(ShiroConstant.SHIRO_CONFIG_COOKIE$TIMEOUT);

        return simpleCookie;
    }

    /** ======================================  CacheManager  ====================================== **/
    /**
     * 本地内存缓存
     * shiro自带的MemoryConstrainedCacheManager作缓存
     * 只能用于本机，集群时无法使用
     * @return
     */
    public CacheManager createMemoryCcacheManager() {
        MemoryConstrainedCacheManager cacheManager=new MemoryConstrainedCacheManager(); //使用内存缓存

        return cacheManager;
    }

    /**
     * EhCache缓存管理器工厂Bean
     * 【 此工厂Bean必需使用动态代理形式才能正常读取xml配置文件并创建cacheManager，
     * 因此需使用注解（@Configuration/@Bean、@Component等），交由Spring容器管理 】
     * @return
     */
    public EhCacheManagerFactoryBean createEhCacheManagerFactoryBean(@NotNull  String xmlResourcePath) {
        EhCacheManagerFactoryBean ehCacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        // xml默认设为resources目录下: ehcache.xml    (classpath:ehcache.xml) 无需classpath:
        ehCacheManagerFactoryBean.setConfigLocation(new ClassPathResource(xmlResourcePath));
        ehCacheManagerFactoryBean.setCacheManagerName("EhCacheManager");
        ehCacheManagerFactoryBean.setShared(true);

        return ehCacheManagerFactoryBean;
    }

    public EhCacheManagerFactoryBean createEhCacheManagerFactoryBean(@NotNull  String xmlResourcePath,
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
    public CacheManager createEhCacheManager(EhCacheManagerFactoryBean ehCacheManagerFactoryBean) {
        EhCacheManager ehCacheManager = new EhCacheManager();
        ehCacheManager.setCacheManager(ehCacheManagerFactoryBean.getObject());

        return ehCacheManager;
    }

    /**
     * CacheManager 缓存管理器
     * 【 使用内部默认的Redis管理器 】
     * @return
     */
    public CacheManager createRedisCacheManager() {
        if (null == defaultRedisManager)
            throw new RuntimeException("defaultRedisManager is null");

        return createRedisCacheManager(defaultRedisManager);
    }

    /**
     * CacheManager 缓存管理器
     * 【 使用自定义的Redis管理器 】
     * @param redisManager
     * @return
     */
    public RedisCacheManager createRedisCacheManager(RedisManager redisManager) {
        if (null == redisManager)
            return null;

        return new RedisCacheManager(redisManager,
                ShiroConstant.SHIRO_REDIS_KEY$PREFIX_CACHE,
                ShiroConstant.SHIRO_CACHE_EXPIRE$IN);
    }

    /**
     * 内部默认Redis管理器
     * @return
     */
    private RedisManager createRedisManager() {
        if (null == redisProperties)
            throw new RuntimeException("redisProperties is null");

        return new RedisManager(redisProperties);
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
    /**
     * 会话RedisDAO
     * 【 使用内部默认RedisManager 】
     * @return
     */
    public SessionDAO createRedisSessionDAO() {
        if (null == defaultRedisManager)
            throw new RuntimeException("defaultRedisManager is null");

        return createRedisSessionDAO(defaultRedisManager);
    }

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

    /** ======================================  AOP  ====================================== **/
    /**
     * 开启cglib代理 （如@RequiresRoles,@RequiresPermissions）
     * 配置以下两个Bean：
     * DefaultAdvisorAutoProxyCreator（可选，无此可能造成无法使用授权注解）
     * AuthorizationAttributeSourceAdvisor
     * （一个Advisor是一个切入点和一个通知的组成，AOP）
     * 【 如果不需要使用授权注解，则可以忽略 】
     * @return
     */
    public DefaultAdvisorAutoProxyCreator createDefaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);

        return proxyCreator;
    }

    /**
     * 开启shiro aop注解支持（使用代理方式，所以需要开启代码支持）,需借助SpringAOP扫描使用Shiro注解的类，并在必要时进行安全逻辑验证
     * 【 如果不需要使用授权注解，则可以忽略 】
     * @param securityManager
     * @return
     */
    public AuthorizationAttributeSourceAdvisor createAuthorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);

        return advisor;
    }

    /** ======================================  Exception  ====================================== **/
    /**
     * Shiro异常捕获跳转
     * 【 简单配置，如果采用Spring默认异常处理，则可以忽略 】
     * @return
     */
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
        Properties properties = new Properties();
        properties.setProperty(
                AuthenticationException.class.getName(),
                ShiroConstant.SHIRO_CONFIG_UNAUTHORIZED$URL);  // 身份认证异常跳转

        properties.setProperty(
                AuthorizationException.class.getName(),
                ShiroConstant.SHIRO_CONFIG_UNAUTHORIZED$URL);  // 授权异常跳转

        return simpleMappingExceptionResolver(properties);
    }

    /**
     * Shiro异常捕获跳转
     * 【 自定义Shiro异常类及跳转配置，如果采用Spring默认异常处理，则可以忽略。如果采用内部实现的异常，则传值null 】
     * @param properties
     * @return
     */
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver(Properties properties) {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();

        if (null != properties)
            simpleMappingExceptionResolver.setExceptionMappings(properties);

        return simpleMappingExceptionResolver;
    }
}
