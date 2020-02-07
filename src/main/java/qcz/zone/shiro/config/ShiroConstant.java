package qcz.zone.shiro.config;

import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 26
 */
public class ShiroConstant {
    public static Boolean DEBUG_LOG = true;    // 默认输出debug日志

    // SHIRO Config
    public static String SHIRO_CONFIG_HASH$ALGORITHM$NAME = "MD5";    // 哈希加密算法名：MD5、SHA-1、SHA-256
    public static Integer SHIRO_CONFIG_HASH$ITERATIONS_1024 = 1024;         // 哈希迭代次数
    public static Long SHIRO_CONFIG_SESSION$TIMEOUT = 3600L;     // session 超时时间（单位：秒）
    public static Integer SHIRO_CONFIG_COOKIE$TIMEOUT = 2592000;      // rememberMe cookie有效时长(秒)，默认30天(2592000L)
    public static String SHIRO_CONFIG_LOGIN$URL = "/login";        // 登录 url
    public static String SHIRO_CONFIG_SUCCESS$URL = "/index";      // 登录成功后跳转的 url
    public static String SHIRO_CONFIG_UNAUTHORIZED$URL = "/403"; // 未授权跳转 url


    public static Integer SHIRO_LOGIN_COUNT = 5;      // SHIRO登录次数计数器，超过5次时锁定帐户（一定）时间
    public static Long SHIRO_LOCK_ACCOUNT_TIME = 60L;   // 锁定帐户时长（单位：分钟）
    public static final TimeUnit SHIRO_LOCK_ACCOUNT_TIME$UNIT = TimeUnit.MINUTES;   // 锁定账户时间单位（分）
    public static final Boolean SHIRO_ACCOUNT_LOCK = true;  // 账户已被锁定
    public static final Boolean SHIRO_ACCOUNT_UNLOCK = false; // 账户未锁定

    public static Long SHIRO_REDIS_EXPIRE$IN = 1800L;           // redis缓存时长（单位：秒）
    public static final TimeUnit SHIRO_REDIS_DEFAULT_TIME$UNIT = TimeUnit.SECONDS;    // shiro默认计时单位（秒）

    // SHIRO Redis key prefix
    public static String SHIRO_REDIS_KEY$PREFIX_LOGIN$COUNTER = "Shiro-login.counter:";   // 登录次数计数器
    public static String SHIRO_REDIS_KEY$PREFIX_ACCOUNT$IS$LOCK = "Shiro-account.lock:";  // 账户是否被锁
    public static String SHIRO_REDIS_KEY$PREFIX_SESSION = "shiro-redis.session:";     // session redis缓存
    public static String SHIRO_REDIS_KEY$PREFIX_CACHE = "shiro-redis.cache:";         // cache redis缓存

    public static void init(ShiroProperties shiroProperties) {
        if (null == shiroProperties)
            return;

        if (!StringUtils.isEmpty(shiroProperties.getHashAlgorithmName()))
            SHIRO_CONFIG_HASH$ALGORITHM$NAME = shiroProperties.getHashAlgorithmName();
        if (null != shiroProperties.getHashIterations() && shiroProperties.getHashIterations() > 0)
            SHIRO_CONFIG_HASH$ITERATIONS_1024 = shiroProperties.getHashIterations();
        if (null != shiroProperties.getSessionTimeout() && shiroProperties.getSessionTimeout() > 0)
            SHIRO_CONFIG_SESSION$TIMEOUT = shiroProperties.getSessionTimeout();
        if (null != shiroProperties.getCookieTimeout() && shiroProperties.getCookieTimeout() > 0)
            SHIRO_CONFIG_COOKIE$TIMEOUT = shiroProperties.getCookieTimeout();
        if (!StringUtils.isEmpty(shiroProperties.getLoginUrl()))
            SHIRO_CONFIG_LOGIN$URL = shiroProperties.getLoginUrl();
        if (!StringUtils.isEmpty(shiroProperties.getSuccessUrl()))
            SHIRO_CONFIG_SUCCESS$URL = shiroProperties.getSuccessUrl();
        if (!StringUtils.isEmpty(shiroProperties.getUnauthorizedUrl()))
            SHIRO_CONFIG_UNAUTHORIZED$URL = shiroProperties.getUnauthorizedUrl();
        if (null != shiroProperties.getLoginCount() && shiroProperties.getLoginCount() > 0)
            SHIRO_LOGIN_COUNT = shiroProperties.getLoginCount();
        if (null != shiroProperties.getLockAccountTime() && shiroProperties.getLockAccountTime() > 0)
            SHIRO_LOCK_ACCOUNT_TIME = shiroProperties.getLockAccountTime();
        if (null != shiroProperties.getExpireIn() && shiroProperties.getExpireIn() > 0)
            SHIRO_REDIS_EXPIRE$IN = shiroProperties.getExpireIn();

        if (null != shiroProperties.getDebugLog())
            DEBUG_LOG = shiroProperties.getDebugLog();
    }
}
