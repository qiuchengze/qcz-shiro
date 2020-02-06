package qcz.zone.shiro.filter;

import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 06
 */

/**
 * Shiro跨域访问拦截器
 * 【 适用简单http请求或复杂http请求 】
 */
public class TransdomainFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 允许哪些Origin发起跨域请求
        String orgin = request.getHeader("Origin");
        // response.setHeader( "Access-Control-Allow-Origin", config.getInitParameter( "AccessControlAllowOrigin" ) );
        response.setHeader( "Access-Control-Allow-Origin", orgin );
        // 允许请求的方法
        response.setHeader( "Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PUT" );
        // 多少秒内,不需要再发送预检验请求，可以缓存该结果
        response.setHeader( "Access-Control-Max-Age", "3600" );
        // 表明它允许跨域请求包含xxx头
        response.setHeader( "Access-Control-Allow-Headers", "x-auth-token, Origin,Access-Token, X-Requested-With, Content-Type, Accept, token" );
        // 是否允许浏览器携带用户身份信息（cookie）
        response.setHeader( "Access-Control-Allow-Credentials", "true" );
        // prefight请求（如：Ajax请求，会先发一个OPTIONS请求）
        if (request.getMethod().equals( "OPTIONS" )) {
            response.setStatus(HttpStatus.OK.value());
            return;
        }

        filterChain.doFilter( servletRequest, response );
    }

    @Override
    public void destroy() {

    }
}
