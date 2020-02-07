package qcz.zone.shiro.manager;

import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 06
 */

/**
 * 无状态Web会话管理器
 * 【 Ajax请求、前后端分离、App、小程序、跨域等无状态场景会话管理 】
 */
public class StatelessWebSessionManager extends DefaultWebSessionManager {
    private static final String HEADER_TOKEN_NAME = "token";
    private static final String REFERENCED_SESSION_ID_SOURCE = "Stateless request";

    /**
     * 从服务器端会话缓存（SessionStore）中，根据其内的key获取会话ID，如果不存在则创建一个会话ID，并返回给客户端
     * @param key
     * @return
     */
    @Override
    public Serializable getSessionId(SessionKey key) {
        Serializable sessionId = key.getSessionId();
        if(null == sessionId){
            HttpServletRequest request = WebUtils.getHttpRequest(key);
            HttpServletResponse response = WebUtils.getHttpResponse(key);
            sessionId = this.getSessionId(request, response);
        }

        HttpServletRequest request = WebUtils.getHttpRequest(key);
        request.setAttribute(HEADER_TOKEN_NAME, sessionId.toString());

        return sessionId;
    }

    /**
     * 从客户端请求头中获取其会话ID，如果不存在则创建一个会话ID，并返回给客户端
     * @param request
     * @param response
     * @return
     */
    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        // 从请求头中获取token（其值为sessionId）
        String sessionId = servletRequest.getHeader(HEADER_TOKEN_NAME);

        // 按默认规则从cookie中获取sessionId
        if(StringUtils.isEmpty(sessionId))
            sessionId = (String) super.getSessionId(request, response);

        // 未获取到sessionId，则创建一个
        if (StringUtils.isEmpty(sessionId))
            sessionId = UUID.randomUUID().toString();

        // REFERENCED_SESSION_ID_SOURCE：设置请求头中cookie属性的key的名称
        servletRequest.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, REFERENCED_SESSION_ID_SOURCE);
        // sessionId属性（关键）
        servletRequest.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, sessionId);
        servletRequest.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);

        return sessionId;
    }
}
