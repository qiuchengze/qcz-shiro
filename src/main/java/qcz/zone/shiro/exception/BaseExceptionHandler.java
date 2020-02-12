package qcz.zone.shiro.exception;

import com.google.common.base.Throwables;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 09
 */
public abstract class BaseExceptionHandler extends ResponseEntityExceptionHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseExceptionHandler.class);
    protected static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    protected static final String DEFAULT_ERROR_MESSAGE = "系统忙，请稍后再试";
    protected static final String DEFAULT_ERROR_VIEW$NAME = "401";


    protected ModelAndView handleError(HttpServletRequest request, HttpServletResponse response, CustomException e) throws Exception {
        HttpStatus httpStatus = HttpStatus.valueOf(e.getCode());
        return handleError(request, response, e, httpStatus, String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    protected ModelAndView handleError(HttpServletRequest request, HttpServletResponse response, Exception e, HttpStatus status) throws Exception {
        return handleError(request, response, e, status, DEFAULT_ERROR_VIEW$NAME);
    }
    
    protected ModelAndView handleError(HttpServletRequest request, HttpServletResponse response, Exception e, HttpStatus status, String viewName) throws Exception {
        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
            throw e;

        String errorMsg = e instanceof CustomException ? e.getMessage() : exceptionMessage(e);
        String errorStack = Throwables.getStackTraceAsString(e);

        ExceptionResponse er = new ExceptionResponse();
        er.setCode(status.value());
        er.setUrl(request.getRequestURL().toString());
        er.setMessage(errorMsg);
        er.setStack(errorStack);
        er.setTimestamp(DATE_FORMAT.format(new Date()));

        LOGGER.error("Request: {} raised {}", request.getRequestURI(), errorStack);
        if (isAjax(request))
            return handleAjaxError(response, er);

        return handleViewError(er, viewName);
    }

    protected String exceptionMessage(Exception e) {
        if (e instanceof IncorrectCredentialsException)
            return "不正确的凭证（密码）";
        else if (e instanceof ExpiredCredentialsException)
            return "凭证过期";
        else if (e instanceof ConcurrentAccessException)
            return "并发访问异常（多个用户同时登录）";
        else if (e instanceof UnknownAccountException)
            return "未知的账号";
        else if (e instanceof ExcessiveAttemptsException)
            return "认证次数超过限制";
        else if (e instanceof DisabledAccountException)
            return "禁用的账号";
        else if (e instanceof LockedAccountException)
            return "账号被锁定";
        else if (e instanceof AuthenticationException)
            return "尚未登录或身份认证失败";
        else if (e instanceof UnauthorizedException)
            return "访问权限不足";
        else if (e instanceof RuntimeException)
            return "系统内部运行时发意外情况，请稍候再试";
        else if (e instanceof Exception)
            return "系统内部发生未知情况，请稍候再试";
        else
            return DEFAULT_ERROR_MESSAGE;
    }

    protected ModelAndView handleViewError(ExceptionResponse er, String viewName) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("exceptionResponse", er);
        mav.setViewName(viewName);

        return mav;
    }

    protected ModelAndView handleAjaxError(HttpServletResponse response, ExceptionResponse er) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setStatus(er.getCode());
        PrintWriter writer = response.getWriter();
        writer.write(er.getMessage());
        writer.flush();

        return null;
    }

    /**
     * 判断是否是Ajax请求
     *
     * @param request
     * @return
     */
    protected boolean isAjax(HttpServletRequest request) {
        return (request.getHeader("X-Requested-With") != null &&
                "XMLHttpRequest".equals(request.getHeader("X-Requested-With").toString()));
    }
}
