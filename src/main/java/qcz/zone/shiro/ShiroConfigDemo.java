package qcz.zone.shiro;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import qcz.zone.shiro.config.AbstractShiroConfig;
import qcz.zone.shiro.config.ShiroProperties;
import qcz.zone.shiro.factory.impl.ShiroEhCacheFactory;
import qcz.zone.shiro.service.ShiroService;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 04
 */

//@Configuration
public class ShiroConfigDemo extends AbstractShiroConfig {
    @Autowired
    private ShiroService shiroService;

    @Autowired
    private ShiroProperties shiroProperties;

    @Autowired
    private ShiroEhCacheFactory shiroFactory;
    
    @Bean(name = "shiroFactory")
    public ShiroEhCacheFactory shiroEhCacheFactory(CacheManager cacheManager) {
        return new ShiroEhCacheFactory(shiroService, cacheManager, shiroProperties);
    }

    @Bean(name = "shiroFilter")
    public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
        return shiroFactory.createShiroFilter(securityManager);
    }

    @Bean(name = "securityManager")
    public SecurityManager securityManager(Realm realm, SessionManager sessionManager) {
        return shiroFactory.createEhCacheSecurityManager(realm, sessionManager);
    }

    @Bean(name = "realm")
    public Realm realm() {
        return shiroFactory.createEhCacheRealm();
    }

    @Bean(name = "sessionManager")
    public SessionManager sessionManager() {
        return shiroFactory.createEhCacheSessionManager();
    }

    @Bean(name = "ehCacheManagerFactoryBean")
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        return ShiroEhCacheFactory.createEhCacheManagerFactoryBean("ehcache.xml");
    }

    @Bean(name = "cacheManager")
    public CacheManager cacheManager(EhCacheManagerFactoryBean ehCacheManagerFactoryBean) {
        return ShiroEhCacheFactory.createEhCacheManager(ehCacheManagerFactoryBean);
    }
}
