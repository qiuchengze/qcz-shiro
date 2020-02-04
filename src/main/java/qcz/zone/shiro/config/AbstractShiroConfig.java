package qcz.zone.shiro.config;

import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import qcz.zone.shiro.ShiroFactory;
import qcz.zone.shiro.realm.ShiroRealm;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 03
 */
public abstract class AbstractShiroConfig {

    /**
     * 创建shiro工厂Bean
     * @return
     */
    protected abstract ShiroFactory shiroFactory();

    /**
     * Shiro拦截器及拦截策略
     * @param shiroFactory  shiro工厂Bean
     * @param securityManager   Web类型安全管理器
     * @return
     */
    protected abstract ShiroFilterFactoryBean shiroFilter(ShiroFactory shiroFactory, DefaultWebSecurityManager securityManager);

    /**
     * Web类型安全管理器
     * @param shiroFactory  shiro工厂Bean
     * @return
     */
    protected abstract DefaultWebSecurityManager securityManager(ShiroFactory shiroFactory,
                                                                 ShiroRealm shiroRealm,
                                                                 DefaultWebSessionManager sessionManager,
                                                                 RememberMeManager rememberMeManager);

    /**
     * Shiro域（用户身份、角色、权限、认证凭证器匹配器等数据源）
     * @param shiroFactory shiro工厂Bean
     * @return
     */
    protected abstract ShiroRealm shiroRealm(ShiroFactory shiroFactory);

    /**
     * Session管理器（会话管理器）
     * @param shiroFactory  shiro工厂Bean
     * @return
     */
    protected abstract DefaultWebSessionManager sessionManager(ShiroFactory shiroFactory);

    /**
     * Cookie管理器（记住我功能）
     * 【 如果不需要，继承后直接返回null 】
     * @param shiroFactory  shiro工厂Bean
     * @return
     */
    protected abstract CookieRememberMeManager rememberMeManager(ShiroFactory shiroFactory);
}
