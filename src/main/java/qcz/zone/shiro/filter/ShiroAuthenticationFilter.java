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
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import qcz.zone.shiro.entity.ShiroUser;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 */
public class ShiroAuthenticationFilter extends AccessControlFilter {
    private String kickoutUrl = "/kickout";     // 踢出后跳转地址
    private Boolean kickoutFirstLast = false;   // fasle==>踢出最早登录的，true==>踢出最后登录的
    private Integer maxSession = 1;     // 同一用户最大会话数，默认1个
    private final String prefix = "kickout:"; // 此拦截器的redis缓存前缀

    private SessionManager sessionManager;
    private Cache<String, Deque<Serializable>> cache;

    public ShiroAuthenticationFilter() {}

    public ShiroAuthenticationFilter(String kickoutUrl, Boolean kickoutFirstLast, Integer maxSession) {
        if (!StringUtils.isEmpty(kickoutUrl))
            this.kickoutUrl = kickoutUrl;

        if (null != kickoutFirstLast)
            this.kickoutFirstLast = kickoutFirstLast;

        if (null != maxSession && maxSession > this.maxSession)
            this.maxSession = maxSession;
    }

    /**
     * 无条件放行OPTIONS
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        // 无条件放行OPTIONS
        if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            setHeader(httpRequest, httpResponse);
            return true;
        }

        return super.preHandle(request, response);
    }

    /**
     * 为response设置header，实现跨域
     * @param request
     * @param response
     */
    private void setHeader(HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST,PUT, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        // 需要放行的Header字段
        response.setHeader("Access-Control-Allow-Headers", "content-type, x-requested-with, token");
//        response.setHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Allow-Headers") + ",token");
        //防止乱码，适用于传输JSON数据
        response.setHeader("Content-Type", "application/json;charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());
    }

    /**
     * 拦截后先进入该方法。返回true，则直接结束当前请求并返回。如果返回false，则交由onAccessDenied处理鉴权与登录逻辑。
     * @param request
     * @param response
     * @param obj
     * @return
     * @throws Exception
     */
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
        ShiroUser shiroUser = (ShiroUser) subject.getPrincipal();
        String principal = String.valueOf(shiroUser.getPrincipal());
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
