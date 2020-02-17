package qcz.zone.shiro.config;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 03
 */
public abstract class AbstractShiroConfig {

    /**
     * Shiro拦截器及拦截策略
     * @param securityManager   Web类型安全管理器
     * @return
     */
    public abstract ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager);

    /**
     * Web类型安全管理器
     * @return
     */
    public abstract SecurityManager securityManager(Realm realm, SessionManager sessionManager);

    /**
     * Shiro域（用户身份、角色、权限、认证凭证器匹配器等数据源）
     * @return
     */
    public abstract Realm realm();

    /**
     * Session管理器（会话管理器）
     * @return
     */
    public abstract SessionManager sessionManager();

}
