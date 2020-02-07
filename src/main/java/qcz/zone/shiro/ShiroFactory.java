package qcz.zone.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.config.ShiroProperties;
import qcz.zone.shiro.entity.ShiroUrlAccessStrategy;
import qcz.zone.shiro.manager.StatelessWebSessionManager;
import qcz.zone.shiro.redis.RedisSessionDAO;
import qcz.zone.shiro.filter.ShiroAuthenticationFilter;
import qcz.zone.shiro.filter.ShiroAuthorizationFilter;
import qcz.zone.shiro.manager.RedisCacheManager;
import qcz.zone.shiro.manager.RedisManager;
import qcz.zone.shiro.realm.ShiroRealm;
import qcz.zone.shiro.redis.RedisShiroLock;
import qcz.zone.shiro.service.ShiroService;
import qcz.zone.shiro.strategy.AbstractLoginStrategy;
import qcz.zone.shiro.strategy.impl.DefaultLoginStrategy;

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
    private AbstractLoginStrategy loginStrategy = null; // 用户登录策略
    private RedisProperties redisProperties = null;     // Redis属性配置
    private RedisManager defaultRedisManager = null;    // 默认Redis管理器

    /**
     * Shiro工厂Bean 构造方法
     * 【 使用内部默认的用户登录策略方式 】
     * @param shiroService
     * @param shiroProperties
     * @param redisProperties
     */
    public ShiroFactory(
            ShiroService shiroService,
            ShiroProperties shiroProperties,
            RedisProperties redisProperties) {
        this(shiroService,
                shiroProperties,
                redisProperties,
                new DefaultLoginStrategy(new RedisShiroLock(redisProperties)));
    }

    /**
     * Shiro工厂Bean 构造方法
     * 【 使用自定义的用户登录策略方式 】
     * @param shiroService
     * @param shiroProperties
     * @param redisProperties
     * @param loginStrategy
     */
    public ShiroFactory(
            ShiroService shiroService,
            ShiroProperties shiroProperties,
            RedisProperties redisProperties,
            AbstractLoginStrategy loginStrategy) {

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

        if (null == redisProperties) {
            throw new RuntimeException("redisProperties is null");
        } else {
            this.redisProperties = redisProperties;
        }

        if (null == loginStrategy) {
            throw new RuntimeException("loginStrategy is null");
        } else {
            this.loginStrategy = loginStrategy;
        }

        ShiroConstant.init(shiroProperties);
        this.defaultRedisManager = createRedisManager();
    }

    /**
     * 创建Shiro拦截器及拦截策略
     * @param securityManager
     * @return
     */
    public ShiroFilterFactoryBean createShiroFilter(DefaultWebSecurityManager securityManager) {
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

    /**
     * Web类型安全管理器
     * 【 1. 使用内部提供的Redis缓存管理器方式 】
     * 【 2. 不使用记住我功能 】
     * @param shiroRealm
     * @param sessionManager
     * @return
     */
    public DefaultWebSecurityManager createSecurityManager(
            @NotNull ShiroRealm shiroRealm,
            @NotNull DefaultWebSessionManager sessionManager
    ) {
        RedisCacheManager redisCacheManager = this.createRedisCacheManager();
        return createSecurityManager(shiroRealm, sessionManager, redisCacheManager, null);
    }

    /**
     * Web类型安全管理器
     * 【 1. 使用自定义Redis缓存管理器方式 或 不使用Redis缓存管理器（不使用传null）】
     * 【 2. 不使用记住我功能 】
     * @param shiroRealm
     * @param sessionManager
     * @param redisCacheManager
     * @return
     */
    public DefaultWebSecurityManager createSecurityManager(
            @NotNull ShiroRealm shiroRealm,
            @NotNull DefaultWebSessionManager sessionManager,
            RedisCacheManager redisCacheManager
    ) {
        return createSecurityManager(shiroRealm, sessionManager, redisCacheManager, null);
    }

    /**
     * Web类型安全管理器
     * 【 1. 使用内部提供的Redis缓存管理器 】
     * 【 2. 使用自定义记住我功能 或 不使用记住我功能（不使用传null） 】
     * @param shiroRealm
     * @param sessionManager
     * @param rememberMeManager
     * @return
     */
    public DefaultWebSecurityManager createSecurityManager(
            @NotNull ShiroRealm shiroRealm,
            @NotNull DefaultWebSessionManager sessionManager,
            RememberMeManager rememberMeManager
    ) {
        if (null == shiroRealm)
            throw new RuntimeException("shiroRealm is null");
        if (null == sessionManager)
            throw new RuntimeException("sessionManager is null");

        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();

        defaultWebSecurityManager.setRealm(shiroRealm);
        defaultWebSecurityManager.setSessionManager(sessionManager);

        RedisCacheManager redisCacheManager = this.createRedisCacheManager();
        if (null != redisCacheManager)
            defaultWebSecurityManager.setCacheManager(redisCacheManager);

        if (null != rememberMeManager)
            defaultWebSecurityManager.setRememberMeManager(rememberMeManager);

        return defaultWebSecurityManager;
    }

    /**
     * Web类型安全管理器
     * 【 1. 使用自定义Redis缓存管理器方式 或 不使用Redis缓存管理器（不使用传null）】
     * 【 2. 使用自定义记住我功能 或 不使用记住我功能（不使用传null） 】
     * @param shiroRealm
     * @param sessionManager
     * @param redisCacheManager
     * @param rememberMeManager
     * @return
     */
    public DefaultWebSecurityManager createSecurityManager(
            @NotNull ShiroRealm shiroRealm,
            @NotNull DefaultWebSessionManager sessionManager,
            RedisCacheManager redisCacheManager,
            RememberMeManager rememberMeManager
    ) {
        if (null == shiroRealm)
            throw new RuntimeException("shiroRealm is null");
        if (null == sessionManager)
            throw new RuntimeException("sessionManager is null");

        DefaultWebSecurityManager defaultWebSecurityManager = new DefaultWebSecurityManager();

        defaultWebSecurityManager.setRealm(shiroRealm);
        defaultWebSecurityManager.setSessionManager(sessionManager);

        if (null != redisCacheManager)
            defaultWebSecurityManager.setCacheManager(redisCacheManager);

        if (null != rememberMeManager)
            defaultWebSecurityManager.setRememberMeManager(rememberMeManager);

        return defaultWebSecurityManager;
    }

    /**
     * Shiro域（用户身份、角色、权限、认证凭证器匹配器等数据源）
     * 【 使用内部提供的凭证匹配器 】
     * @return
     */
    public ShiroRealm createShiroRealm() {
        CredentialsMatcher credentialsMatcher = createHashedCredentialsMatcher();
        return createShiroRealm(credentialsMatcher);
    }

    /**
     * Shiro域（用户身份、角色、权限、认证凭证器匹配器等数据源）
     * 【 使用自定义的凭证匹配器 】
     * @param credentialsMatcher
     * @return
     */
    public ShiroRealm createShiroRealm(@NotNull CredentialsMatcher credentialsMatcher) {
        if (null == shiroService)
            throw new RuntimeException("shiroService is null");
        if (null == loginStrategy)
            throw new RuntimeException("loginStrategy is null");
        if (null == credentialsMatcher)
            throw new RuntimeException("credentialsMatcher is null");

        ShiroRealm shiroRealm = new ShiroRealm(shiroService, loginStrategy);
        shiroRealm.setCredentialsMatcher(credentialsMatcher);

        return shiroRealm;
    }

    /**
     * Session管理器（会话管理器）
     * 【 使用内部提供的Redis会话管理器 】
     * @return
     */
    public DefaultWebSessionManager createSessionManager() {
        RedisSessionDAO redisSessionDAO = createRedisSessionDAO();
        return createSessionManager(redisSessionDAO);
    }

    /**
     * 无状态Web会话管理器
     * 【 使用内部提供的Redis会话管理器 】
     * @return
     */
    public DefaultWebSessionManager createStatelessSessionManager() {
        RedisSessionDAO redisSessionDAO = createRedisSessionDAO();
        return createStatelessSessionManager(redisSessionDAO);
    }

    /**
     * 无状态Web会话管理器
     * 【 如不使用Redis来缓存会话，则redisSessionDAO传null 】
     * @param redisSessionDAO
     * @return
     */
    public DefaultWebSessionManager createStatelessSessionManager(RedisSessionDAO redisSessionDAO) {
        return createSessionManager(new StatelessWebSessionManager(), redisSessionDAO);
    }

    /**
     * 默认Web会话管理器（默认Web类型会话管理器）
     * 【 如不使用Redis来缓存会话，则redisSessionDAO传null 】
     * @param redisSessionDAO
     * @return
     */
    public DefaultWebSessionManager createSessionManager(RedisSessionDAO redisSessionDAO) {
        return createSessionManager(new DefaultWebSessionManager(), redisSessionDAO);
    }

    /**
     * 自定义会话管理器
     * 【 如不使用Redis来缓存会话，则redisSessionDAO传null 】
     * @param sessionManager
     * @param redisSessionDAO
     * @return
     */
    public DefaultWebSessionManager createSessionManager(DefaultWebSessionManager sessionManager, RedisSessionDAO redisSessionDAO){
        if (null == sessionManager)
            throw new RuntimeException("sessionManager is null");

        // 缓存超时时间
        sessionManager.setGlobalSessionTimeout(ShiroConstant.SHIRO_CONFIG_SESSION$TIMEOUT);
        // 定时清理失效会话, 清理用户直接关闭浏览器造成的孤立会话
        // 缓存清理间隔时间
        sessionManager.setSessionValidationInterval(ShiroConstant.SHIRO_CONFIG_SESSION$TIMEOUT);
        // 允许删除无效的会话
        sessionManager.setDeleteInvalidSessions(true);
        // 开启周期清理
        sessionManager.setSessionValidationSchedulerEnabled(true);

        /**
         * 是否将SessionId写入Cookie
         * true==>写入：有状态环境（如：PC浏览器Web环境），此环境如果非true，则无法记录登录状态。
         * false==>不写入：无状态环境，如：APP）
         * 【 双模式，增加业务判断进行自适应动态设置 】
         */
        sessionManager.setSessionIdCookieEnabled(true);
//        sessionManager.setSessionIdCookie(sessionIdCookie);    // 注入cookie
        sessionManager.setSessionIdUrlRewritingEnabled(false);  // 去掉shiro登录时url里的JSESSIONID

        // 注入自定义Session持久化器
        if (null != redisSessionDAO)
            sessionManager.setSessionDAO(redisSessionDAO);

        return sessionManager;
    }

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

    /**
     * 内部默认Redis管理器
     * @return
     */
    private RedisManager createRedisManager() {
        if (null == redisProperties)
            throw new RuntimeException("redisProperties is null");

        return new RedisManager(redisProperties);
    }

    /**
     * CacheManager 缓存管理器
     * 【 使用内部默认的Redis管理器 】
     * @return
     */
    public RedisCacheManager createRedisCacheManager() {
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
                ShiroConstant.SHIRO_REDIS_EXPIRE$IN);
    }

    /**
     * 会话RedisDAO
     * 【 使用内部默认RedisManager 】
     * @return
     */
    public RedisSessionDAO createRedisSessionDAO() {
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
    public RedisSessionDAO createRedisSessionDAO(RedisManager redisManager) {
        if (null == redisManager)
            return null;

        return new RedisSessionDAO(
                redisManager,
                ShiroConstant.SHIRO_REDIS_KEY$PREFIX_SESSION,
                ShiroConstant.SHIRO_REDIS_EXPIRE$IN);
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

    /**
     * 开启Shiro的注解（如@RequiresRoles,@RequiresPermissions），需借助SpringAOP扫描使用Shiro注解的类，并在必要时进行安全逻辑验证
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
     * Shiro的注解解释（无此，无法使用授权注解）
     * 【 如果不需要使用授权注解，则可以忽略 】
     * @param securityManager
     * @return
     */
    public AuthorizationAttributeSourceAdvisor createAuthorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);

        return advisor;
    }

    /**
     * Shiro异常捕获跳转
     * 【 如果采用Spring默认异常处理，则可以忽略 】
     * @return
     */
    public SimpleMappingExceptionResolver simpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver simpleMappingExceptionResolver = new SimpleMappingExceptionResolver();

        Properties properties = new Properties();
        properties.setProperty(
                AuthenticationException.class.getName(),
                ShiroConstant.SHIRO_CONFIG_UNAUTHORIZED$URL);  // 身份认证异常跳转

        properties.setProperty(
                AuthorizationException.class.getName(),
                ShiroConstant.SHIRO_CONFIG_UNAUTHORIZED$URL);  // 授权异常跳转

        simpleMappingExceptionResolver.setExceptionMappings(properties);

        return simpleMappingExceptionResolver;
    }
}
