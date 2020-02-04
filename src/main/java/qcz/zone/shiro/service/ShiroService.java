package qcz.zone.shiro.service;

import qcz.zone.shiro.entity.AbstractUrlAccessStrategy;
import qcz.zone.shiro.entity.AbstractUser;
import qcz.zone.shiro.entity.AbstractUserAuths;

import java.util.List;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 02
 */

public interface ShiroService {
    AbstractUser getAbstractUser(Object principal, String password);    // 根据用户唯一身份及密码获取用户信息
    AbstractUserAuths getUserAuths(Object principal);          // 根据用户唯一身份标识获取用户角色和权限集合
    List<AbstractUrlAccessStrategy> getAllUrlAccessStrategy();          // 获取所有地址配置的过滤器及访问权限策略
}
