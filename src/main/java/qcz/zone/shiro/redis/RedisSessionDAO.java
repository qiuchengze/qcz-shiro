package qcz.zone.shiro.redis;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import qcz.zone.shiro.config.ShiroConstant;
import qcz.zone.shiro.manager.RedisManager;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 29
 */
public class RedisSessionDAO extends AbstractSessionDAO {

    private RedisManager redisManager = null;
    private String prefix = "shiro-redis.session:";
    private Long ttl = ShiroConstant.SHIRO_CONFIG_SESSION$TIMEOUT;

    public RedisSessionDAO(RedisManager redisManager) {
        this(redisManager, null,null);
    }

    public RedisSessionDAO(RedisManager redisManager, String prefix) {
        this(redisManager, prefix, null);
    }

    public RedisSessionDAO(RedisManager redisManager, String prefix, Long ttl) {
        if (null == redisManager)
            throw new RuntimeException("redisManager is null");

        this.redisManager = redisManager;

        if (null != prefix)
            this.prefix = prefix;

        if (null != ttl && ttl > 0)
            this.ttl = ttl;
    }

    private String getKey(Serializable sessionId) {
        return this.prefix + sessionId;
    }

    @Override
    protected Serializable doCreate(Session session) {
        if (null == session)
            throw new SessionException("session is null");

        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);

        redisManager.set(getKey(sessionId), session, ttl);

        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        if (null == sessionId)
            throw new SessionException("session id is null");

        Session session = (Session) redisManager.get(getKey(sessionId));

        return session;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        if (session instanceof ValidatingSession && !((ValidatingSession) session).isValid()) {
            delete(session);
            return;
        }

        if (null == session || null == session.getId())
            throw new UnknownSessionException("session or session id is null");

        redisManager.set(getKey(session.getId()), session, ttl);
    }

    @Override
    public void delete(Session session) {
        if (null == session || null == session.getId())
            throw new UnknownSessionException("session or session id is null");

        redisManager.del(getKey(session.getId()));
    }

    @Override
    public Collection<Session> getActiveSessions() {
        return redisManager.vals(getKey("*"));
    }
}
