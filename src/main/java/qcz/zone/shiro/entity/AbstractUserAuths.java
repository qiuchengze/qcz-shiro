package qcz.zone.shiro.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 02
 */
public abstract class AbstractUserAuths implements Serializable {
    public Object principal;       // 用户唯一身份标识，如登录时的手机号，用户名等
    public List<String> roles; // 用户角色集合
    public List<String> perms; // 用户权限集合

    public Object getPrincipal() {
        return principal;
    }

    public void setPrincipal(Object principal) {
        this.principal = principal;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPerms() {
        return perms;
    }

    public void setPerms(List<String> perms) {
        this.perms = perms;
    }
}
