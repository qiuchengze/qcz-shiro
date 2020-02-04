package qcz.zone.shiro.strategy.impl;

import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.beans.factory.annotation.Autowired;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.entity.AbstractUser;
import qcz.zone.shiro.redis.RedisShiroLock;
import qcz.zone.shiro.strategy.AbstractLoginStrategy;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 02
 */

public class DefaultLoginStrategy implements AbstractLoginStrategy {
    private RedisShiroLock redisShiroLock = null;

    public DefaultLoginStrategy(RedisShiroLock redisShiroLock) {
        if (null == redisShiroLock)
            throw new RuntimeException("redisShiroLock is null");

        this.redisShiroLock = redisShiroLock;
    }

    @Override
    public boolean isAllowed(AbstractUser user) {
        if (null == user)
            throw new RuntimeException("user is null");

        String principal = String.valueOf(user.getPrincipal());
        if (null == principal)
            throw new RuntimeException("principal is null");

        if (null == redisShiroLock)
            throw new RuntimeException("redisShiroLock is null");

        // 判断账户是否被锁
        Boolean lock = (Boolean) redisShiroLock.isLock(principal);
        if (null != lock && ShiroConstant.SHIRO_ACCOUNT_LOCK == lock)
            throw new DisabledAccountException("由于密码输入错误次数大于5次，帐号已被锁定 " +
                    ShiroConstant.SHIRO_LOCK_ACCOUNT_TIME + " 分钟，期间禁止登录！");

        // 访问一次，登录次数计数器加1
        redisShiroLock.incrCounter(principal);

        if (null == user)
            throw new UnknownAccountException("帐号或密码不正确！");
//        if (UserStatusEM.NORMAL.getValue() != user.getStatus())
//            throw new DisabledAccountException("帐号已被锁或禁用！");

        // 登录成功，清除计数器
        redisShiroLock.clearCounter(principal);

        return true;
    }
}
