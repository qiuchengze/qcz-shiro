package qcz.zone.shiro.manager;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

import javax.validation.constraints.NotNull;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 17
 */

public class ShiroSecurityManagerBiulder {
    public static InnerClass Builder = new InnerClass();

    public static class InnerClass {
        private DefaultWebSecurityManager INSTANCE = new DefaultWebSecurityManager();

        public InnerClass setRealm(@NotNull Realm realm) {
            INSTANCE.setRealm(realm);

            return this;
        }

        public InnerClass setSessionManager(@NotNull SessionManager sessionManager) {
            INSTANCE.setSessionManager(sessionManager);

            return this;
        }

        public InnerClass setCacheManager(CacheManager cacheManager) {
            if (null != cacheManager)
                INSTANCE.setCacheManager(cacheManager);

            return this;
        }

        public InnerClass setRememberMeManager(RememberMeManager rememberMeManager) {
            if (null != rememberMeManager)
                INSTANCE.setRememberMeManager(rememberMeManager);

            return this;
        }

        public SecurityManager build() {
            return INSTANCE;
        }
    }
}
