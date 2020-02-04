package qcz.zone.shiro;

import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import qcz.zone.shiro.config.AbstractShiroConfig;
import qcz.zone.shiro.config.ShiroProperties;
import qcz.zone.shiro.realm.ShiroRealm;
import qcz.zone.shiro.service.ShiroService;

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

    @Bean
    protected ShiroFactory shiroFactory() {
        return new ShiroFactory(shiroService, shiroProperties, redisProperties);
    }

    @Bean
    protected ShiroFilterFactoryBean shiroFilter(ShiroFactory shiroFactory, DefaultWebSecurityManager defaultWebSecurityManager) {
        return shiroFactory.createShiroFilter(defaultWebSecurityManager);
    }

    @Bean
    protected DefaultWebSecurityManager securityManager(ShiroFactory shiroFactory, ShiroRealm shiroRealm, DefaultWebSessionManager defaultWebSessionManager, RememberMeManager rememberMeManager) {
        return shiroFactory.createSecurityManager(shiroRealm, defaultWebSessionManager);
    }

    @Bean
    protected ShiroRealm shiroRealm(ShiroFactory shiroFactory) {
        return shiroFactory.createShiroRealm();
    }

    @Bean
    protected DefaultWebSessionManager sessionManager(ShiroFactory shiroFactory) {
        return shiroFactory.createSessionManager();
    }

    @Bean
    protected CookieRememberMeManager rememberMeManager(ShiroFactory shiroFactory) {
        return shiroFactory.createRememberMeManager();
    }
}
