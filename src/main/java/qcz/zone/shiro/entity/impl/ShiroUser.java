package qcz.zone.shiro.entity.impl;

import qcz.zone.shiro.entity.AbstractUser;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 03
 */

public class ShiroUser extends AbstractUser {
    public ShiroUser(Object principal, String password) {
        this.principal = principal;
        this.password = password;
    }
}
