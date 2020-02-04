package qcz.zone.shiro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 23
 */

@ConfigurationProperties(prefix = "spring.shiro")
public class ShiroProperties {
    private String hashAlgorithmName = "MD5";   // 哈希加密算法名：MD5、SHA-1、SHA-256
    private Integer hashIterations = 1024;         // 哈希迭代次数

    private Long sessionTimeout = 1800000L;     // session 超时时间，默认1800000毫秒
    private Integer cookieTimeout = 2592000;      // rememberMe cookie有效时长(秒)，默认30天(2592000)
    private String loginUrl = "/login";        // 登录 url
    private String successUrl = "/index";      // 登录成功后跳转的 url
    private String unauthorizedUrl = "/403"; // 未授权跳转 url
    private Integer loginCount = 5;        // SHIRO登录次数计数器，超过5次时锁定帐户（一定）时间
    private Long lockAccountTime = 60L;    // 锁定帐户时长（单位：分钟）
    private Long expireIn = 1800L;           // redis缓存时长（单位：秒）
    private Boolean debugLog = true;       // debug日志是否打印

    public String getHashAlgorithmName() {
        return hashAlgorithmName;
    }

    public void setHashAlgorithmName(String hashAlgorithmName) {
        this.hashAlgorithmName = hashAlgorithmName;
    }

    public Integer getHashIterations() {
        return hashIterations;
    }

    public void setHashIterations(Integer hashIterations) {
        this.hashIterations = hashIterations;
    }

    public Long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Integer getCookieTimeout() {
        return cookieTimeout;
    }

    public void setCookieTimeout(Integer cookieTimeout) {
        this.cookieTimeout = cookieTimeout;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getUnauthorizedUrl() {
        return unauthorizedUrl;
    }

    public void setUnauthorizedUrl(String unauthorizedUrl) {
        this.unauthorizedUrl = unauthorizedUrl;
    }

    public Integer getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(Integer loginCount) {
        this.loginCount = loginCount;
    }

    public Long getLockAccountTime() {
        return lockAccountTime;
    }

    public void setLockAccountTime(Long lockAccountTime) {
        this.lockAccountTime = lockAccountTime;
    }

    public Long getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(Long expireIn) {
        this.expireIn = expireIn;
    }

    public Boolean getDebugLog() {
        return debugLog;
    }

    public void setDebugLog(Boolean debugLog) {
        this.debugLog = debugLog;
    }
}