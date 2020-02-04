package qcz.zone.shiro.strategy;

import qcz.zone.shiro.entity.AbstractUser;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 02
 */
public interface AbstractLoginStrategy {
    // 入参为从数据库中读取的用户账户信息（用于根据用户信息进行业务判定，如果无需进行用户信息判定，则传null）
    boolean isAllowed(AbstractUser user);
}
