package qcz.zone.shiro.entity;

import java.io.Serializable;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 02
 */
public abstract class AbstractUser implements Serializable {
    public Object principal;       // 用户唯一身份标识，可以为用户登录时的手机号，用户名等的副本
    public String password;  // 密码

    public Object getPrincipal() {
        return principal;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
