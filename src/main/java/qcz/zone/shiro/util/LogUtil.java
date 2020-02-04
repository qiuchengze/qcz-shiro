package qcz.zone.shiro.util;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 02 - 02
 */

public class LogUtil {
    private static final String LOG_PREFIX = "【QczShiro】====》";

    public static String create(String tag, String info) {
        return LOG_PREFIX + tag + "：" + info;
    }

    public static String create(String tag, Exception e) {
        return LOG_PREFIX + tag + "：" + e.getMessage();
    }
}
