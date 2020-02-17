package qcz.zone.shiro.realm;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.entity.ShiroUser;
import qcz.zone.shiro.entity.ShiroUserAuths;
import qcz.zone.shiro.service.ShiroService;
import qcz.zone.shiro.strategy.AbstractLoginStrategy;
import qcz.zone.shiro.util.ConvertUtil;
import qcz.zone.shiro.util.DesUtil;
import qcz.zone.shiro.util.LogUtil;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 23
 */

public class ShiroRealm extends AuthorizingRealm {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroRealm.class);

    private ShiroService shiroService = null;
    private AbstractLoginStrategy loginStrategy = null;

    public ShiroRealm(ShiroService shiroService, AbstractLoginStrategy loginStrategy) {
        if (null == shiroService) {
            LOGGER.error(LogUtil.create("init ShiroRealm", "shiroService is null"));
            throw new RuntimeException("shiroService is null");
        }

        this.shiroService = shiroService;
        this.loginStrategy = loginStrategy;

        if (ShiroConstant.DEBUG_LOG)
            LOGGER.debug(LogUtil.create("init ShiroRealm", "success"));
    }

    @Override
    public String getName() {
        return "QczShiroRealm";
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        // 设置只支持UsernamePasswordToken（用户和密码）类型的token
        return token instanceof UsernamePasswordToken;
    }

    /**
     * 用户授权
     * @param principal
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
        if (null == principal)
            return null;

        ShiroUser shiroUser = (ShiroUser) principal.getPrimaryPrincipal();
        if (null == shiroUser)
            return null;

        ShiroUserAuths shiroUserAuths = shiroService.getUserAuths(shiroUser.getPrincipal());
        if (null == shiroUserAuths)
            return null;

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        if (!CollectionUtils.isEmpty(shiroUserAuths.getRoles()))
            authorizationInfo.setRoles(ConvertUtil.List2Set(shiroUserAuths.getRoles()));
        if (!CollectionUtils.isEmpty(shiroUserAuths.getPerms()))
            authorizationInfo.setStringPermissions(ConvertUtil.List2Set(shiroUserAuths.getPerms()));

        return authorizationInfo;
    }

    /**
     * 用户身份认证
     * @param authcToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        if (null == authcToken)
            return null;

        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;
        Object principal = token.getPrincipal();    // 获取当前subject的用户唯一标识（可以是：mobile、id、email等）
//        String uid = token.getUsername();     // 与principal相同，只是将其转为String
        String password = String.valueOf(token.getPassword());
        if (null == principal || StringUtils.isEmpty(password))
            return null;

        ShiroUser user = shiroService.getAbstractUser(principal, DesUtil.hashSaltMd5(password, principal));

        ShiroUser shiroUser = null;
        if (null != user) {
            if (null == user.getPrincipal())
                user.setPrincipal(principal);

            shiroUser = new ShiroUser(user.getPrincipal(), user.getPassword());
        }

        boolean isAllowed = false;
        if (null != loginStrategy) {
            isAllowed = loginStrategy.isAllowed(shiroUser);  // 用户登录检验限制策略
        } else {
            isAllowed = (null == shiroUser ? false : true);
        }

        if (!isAllowed)
            throw new AuthenticationException("身份认证未通过");

        return new SimpleAuthenticationInfo(
                shiroUser,
                shiroUser.getPassword(),
                ByteSource.Util.bytes(String.valueOf(shiroUser.getPrincipal())),
                getName());
    }

}
