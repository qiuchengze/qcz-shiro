package qcz.zone.shiro;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import qcz.zone.shiro.config.AbstractShiroConfig;
import qcz.zone.shiro.config.ShiroProperties;
import qcz.zone.shiro.realm.ShiroRealm;
import qcz.zone.shiro.service.ShiroService;
import qcz.zone.shiro.strategy.impl.DefaultEhcacheLoginStrategy;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 04
 */

//@Configuration
//@ConditionalOnClass(value = {ShiroService.class, ShiroProperties.class, RedisProperties.class})
public class ShiroConfigDemo extends AbstractShiroConfig {
    @Autowired
    private ShiroService shiroService;

    @Autowired
    private ShiroProperties shiroProperties;

    @Autowired
    private RedisProperties redisProperties;

    @Autowired
    private ShiroFactory shiroFactory;

    @Autowired
    private CacheManager cacheManager;

    @Bean
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        return shiroFactory.createShiroFilter(securityManager);
    }

    @Bean
    public SecurityManager securityManager(Realm realm, SessionManager sessionManager) {
        return shiroFactory.createDefaultWebSecurityManager(realm, sessionManager, null, null);
    }

    @Bean
    public Realm realm() {
        return shiroFactory.createRealm(new DefaultEhcacheLoginStrategy(cacheManager));
    }

    @Bean
    public SessionManager sessionManager() {
        return shiroFactory.createDefaultWebSessionManager(shiroFactory.createRedisSessionDAO());
    }

    /**
     * 缓存管理器
     * 【 EhCacheManager缓存管理器 】
     * @return
     */
    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        return shiroFactory.createEhCacheManagerFactoryBean("ehcache.xml");
    }

    @Bean
    public CacheManager cacheManager(EhCacheManagerFactoryBean ehCacheManagerFactoryBean) {
        return shiroFactory.createEhCacheManager(ehCacheManagerFactoryBean);
    }
}
