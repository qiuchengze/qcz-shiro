package qcz.zone.shiro.filter;

import com.alibaba.fastjson.JSON;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.StringUtils;
import qcz.zone.shiro.entity.AbstractUser;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 30
 */

/**
 * 自定义身份认证拦截器
 * 对登录次数
 */
public class AuthenticationFilter extends AccessControlFilter {
    private String kickoutUrl = "/kickout";     // 踢出后跳转地址
    private Boolean kickoutFirstLast = false;   // fasle==>踢出最早登录的，true==>踢出最后登录的
    private Integer maxSession = 1;     // 同一用户最大会话数，默认1个
    private final String prefix = "kickout:"; // 此拦截器的redis缓存前缀

    private SessionManager sessionManager;
    private Cache<String, Deque<Serializable>> cache;

    public AuthenticationFilter() {}

    public AuthenticationFilter(String kickoutUrl, Boolean kickoutFirstLast, Integer maxSession) {
        if (!StringUtils.isEmpty(kickoutUrl))
            this.kickoutUrl = kickoutUrl;

        if (null != kickoutFirstLast)
            this.kickoutFirstLast = kickoutFirstLast;

        if (null != maxSession && maxSession > this.maxSession)
            this.maxSession = maxSession;
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object obj) throws Exception {
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        Subject subject = getSubject(request, response);

        // 如果没有登录，直接进行之后的流程
        if (!subject.isAuthenticated() || !subject.isRemembered())
            return true;

        Session session = subject.getSession();
        AbstractUser user = (AbstractUser) subject.getPrincipal();
        String principal = String.valueOf(user.getPrincipal());
        Serializable sessionId = session.getId();

        Deque<Serializable> deque = null;
        if (null != cache)
            deque = cache.get(principal);

        if (null == deque)
            deque = new LinkedList<Serializable>();

        Boolean kickout = (Boolean) session.getAttribute("kickout");
        if (!deque.contains(sessionId) && null == kickout) {
            deque.push(sessionId);
            cache.put(principal, deque);
        }

        while(deque.size() > maxSession) {
            Serializable kickoutSessionId = null;
            if (kickoutFirstLast) {
                kickoutSessionId = deque.removeFirst();
            } else {
                kickoutSessionId = deque.removeLast();
            }

            cache.put(principal, deque);

            Session kickoutSession = sessionManager.getSession(new DefaultSessionKey(kickoutSessionId));
            if (null != kickoutSession)
                kickoutSession.setAttribute("kickout", true);
        }


        if (null != kickout && kickout) {
            subject.logout();
            saveRequest(request);

            Map<String, String> resultMap = new HashMap<String, String>();
            //判断是不是Ajax请求
            if ("XMLHttpRequest".equalsIgnoreCase(((HttpServletRequest) request).getHeader("X-Requested-With"))) {
                resultMap.put("user_status", "300");
                resultMap.put("message", "您已经在其他地方登录，请重新登录！");
                //输出json串
                out(response, resultMap);
            }else{
                //重定向
                WebUtils.issueRedirect(request, response, kickoutUrl);
            }
            return false;
        }

        return false;
    }

    private void out(ServletResponse response, Map<String, String> resultMap)
            throws IOException {
        try {
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.println(JSON.toJSONString(resultMap));
            out.flush();
            out.close();
        } catch (Exception e) {
            System.err.println("KickoutSessionFilter.class 输出JSON异常，可以忽略。");
        }
    }

    public String getKickoutUrl() {
        return kickoutUrl;
    }

    public void setKickoutUrl(String kickoutUrl) {
        if (!StringUtils.isEmpty(kickoutUrl))
            this.kickoutUrl = kickoutUrl;
    }

    public Boolean getKickoutFirstLast() {
        return kickoutFirstLast;
    }

    public void setKickoutFirstLast(Boolean kickoutFirstLast) {
        if (null != kickoutFirstLast)
            this.kickoutFirstLast = kickoutFirstLast;
    }

    public Integer getMaxSession() {
        return maxSession;
    }

    public void setMaxSession(Integer maxSession) {
        if (null != maxSession && maxSession > this.maxSession)
            this.maxSession = maxSession;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    public void setCache(CacheManager cacheManager) {
        this.cache = cacheManager.getCache(prefix);
    }
}
