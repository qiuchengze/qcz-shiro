package qcz.zone.shiro.exception;

import org.apache.shiro.authc.*;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 09
 */

@ControllerAdvice
public class ShiroExceptionHandler extends BaseExceptionHandler {

    @ExceptionHandler(value = {
            IncorrectCredentialsException.class,
            ExpiredCredentialsException.class,
            ConcurrentAccessException.class,
            UnknownAccountException.class,
            ExcessiveAttemptsException.class,
            DisabledAccountException.class,
            LockedAccountException.class,
            AuthenticationException.class,
            UnauthorizedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ModelAndView exceptionAuthentication(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
        return handleError(request, response, e, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {RuntimeException.class, Exception.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView exception(HttpServletRequest request, HttpServletResponse response, Exception e) throws Exception {
        return handleError(request, response, e, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {CustomException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView customException(HttpServletRequest request, HttpServletResponse response, CustomException e) throws Exception {
        return handleError(request, response, e);
    }

}
