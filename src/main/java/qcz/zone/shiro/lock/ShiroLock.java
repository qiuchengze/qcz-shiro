package qcz.zone.shiro.lock;

import qcz.zone.shiro.config.ShiroConstant;

import java.util.concurrent.TimeUnit;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 16
 */
public abstract class ShiroLock<K> {
    private int loginCount = ShiroConstant.SHIRO_LOGIN_COUNT;                                   // 允许登录重试次数
    private String loginCounterPrefix = ShiroConstant.SHIRO_REDIS_KEY$PREFIX_LOGIN$COUNTER;     // 登录失败次数计数器缓存key前缀
    private String accountLockPrefix = ShiroConstant.SHIRO_REDIS_KEY$PREFIX_ACCOUNT$IS$LOCK;    // 用户账户锁缓存key前缀
    private long lockTime = ShiroConstant.SHIRO_LOCK_ACCOUNT_TIME;                              // 锁定账户时长
    private TimeUnit lockTimeUnit = ShiroConstant.SHIRO_LOCK_ACCOUNT_TIME$UNIT;                 // 锁时间单位

    protected String getCounterKey(K k) {
        if (null == k)
            throw new RuntimeException("param is null");

        return loginCounterPrefix + k;
    }

    protected String getLockKey(K k) {
        if (null == k)
            throw new RuntimeException("param is null");

        return accountLockPrefix + k;
    }

    protected boolean isLimitReached(Integer incr) {
        if (null == incr)
            return false;

        if (incr >= loginCount)
            return true;

        return false;
    }

    protected void clear(K k) {
        delLock(k);
        delCounter(k);
    }

    public int getLoginCount() {
        return loginCount;
    }

    public void setLoginCount(int loginCount) {
        this.loginCount = loginCount;
    }

    public String getLoginCounterPrefix() {
        return loginCounterPrefix;
    }

    public void setLoginCounterPrefix(String loginCounterPrefix) {
        this.loginCounterPrefix = loginCounterPrefix;
    }

    public String getAccountLockPrefix() {
        return accountLockPrefix;
    }

    public void setAccountLockPrefix(String accountLockPrefix) {
        this.accountLockPrefix = accountLockPrefix;
    }

    public long getLockTime() {
        return lockTime;
    }

    public void setLockTime(long lockTime) {
        this.lockTime = lockTime;
    }

    public TimeUnit getLockTimeUnit() {
        return lockTimeUnit;
    }

    public void setLockTimeUnit(TimeUnit lockTimeUnit) {
        this.lockTimeUnit = lockTimeUnit;
    }

    public abstract boolean hasCounter(K k);
    public abstract boolean hasLock(K k);

    // 计数器加1
    public abstract void incrCounter(K k);
    public abstract void delCounter(K k);

    public abstract boolean isLock(K k);
    public abstract void setLock(K k);
    public abstract void delLock(K k);

}
