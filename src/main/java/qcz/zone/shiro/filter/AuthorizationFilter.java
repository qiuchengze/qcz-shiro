package qcz.zone.shiro.filter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.subject.Subject;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 30
 */

/**
 * 自定义授权拦截器
 */
public class AuthorizationFilter extends org.apache.shiro.web.filter.authz.AuthorizationFilter {

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object obj) throws Exception {
        Set<String> setPerms = new HashSet<String>();
        // 获取当前地址所要求的权限集合（如：@RequiresPermissions注解中value参数指定的权限集，或数据库url_filter表中设置的url所的perms权限）
        CollectionUtils.addAll(setPerms, (String[]) obj);

        // 当前访问的地址没有设置权限，直接放行
        if (CollectionUtils.isEmpty(setPerms))
            return true;

        Subject subject = getSubject(request, response);

        // 只要具备其中某一权限即可放行
        for (String perm : setPerms) {
            if (subject.isPermitted(perm))
                return true;
        }

        return false;
    }
}
