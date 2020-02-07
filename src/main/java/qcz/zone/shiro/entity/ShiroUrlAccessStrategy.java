package qcz.zone.shiro.entity;

import java.io.Serializable;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 02
 */
public class ShiroUrlAccessStrategy implements Serializable {
    private String url = null;              // 目标地址（如：/, /index, /login, /static/**, /**）
    private String filters = null;          // 为目标地址配置的过滤器名或过滤器名及所需权限，多个过滤器或权限以逗号分隔（如：anon，perms[add]，perms[add, del]，roles[admin]，authc）
    private Integer priorities = null;      // 优先级编号（数值越小，优先级越高，如：1~9，根据数值从小到大顺序装入过滤器链中）

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFilters() {
        return filters;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public Integer getPriorities() {
        return priorities;
    }

    public void setPriorities(Integer priorities) {
        this.priorities = priorities;
    }
}
