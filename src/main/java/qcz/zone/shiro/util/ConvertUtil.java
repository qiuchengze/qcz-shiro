package qcz.zone.shiro.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 10
 */
public class ConvertUtil {

    /**
     * List<String> 转 Set<String>
     * @param lstStr
     * @return
     */
    public static Set<String> List2Set(List<String> lstStr) {
        return new HashSet<String>(lstStr);
    }

    /**
     * JavaObject 转 Map
     * @param obj
     * @return
     */
    public static Map<String, Object> Object2Map(Object obj) {
        Class clazz = obj.getClass();

        Field[] fields = clazz.getDeclaredFields();
        if (null == fields || 0 == fields.length)
            return null;

        Map<String, Object> mapObj = new HashMap<String, Object>();

        for (Field field : fields) {
            field.setAccessible(true);

            String key = field.getName();
            Object val = null;
            try {
                val = field.get(obj);
                if (null != val)
                    mapObj.put(key, val);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return mapObj;
    }

    /**
     * JSON字符串 转 JavaObject
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T Jstr2Object(String str, Class<T> clazz) {
        if (null == str || null == clazz)
            return null;

        return Json2Object(JSON.parseObject(str), clazz);
    }

    /**
     * JSONObject 转 JavaObject
     * @param json
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T Json2Object(JSONObject json, Class<T> clazz) {
        if (null == json || null == clazz)
            return null;

        return JSONObject.toJavaObject(json, clazz);
    }

    /**
     * JSONStringObject 转 JavaObject
     * HttpClient请求返回消息体（body）中的嵌套对象或复杂对象（JSON字符串形式的Object）转换为JavaObject
     * @param obj
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T JstrObj2Object(Object obj, Class<T> clazz) {
        if (null == obj || null == clazz)
            return null;

        String jsonStr = JSON.toJSONString(obj);

        return Jstr2Object(jsonStr, clazz);
    }

    /**
     * JSONStringObject 转 JSONArray
     * HttpClient请求返回消息体（body）中的嵌套对象或复杂对象（JSON字符串形式的Object）转换为JavaObject
     * @param obj
     * @return
     */
    public static JSONArray JstrObj2JSONArray(Object obj) {
        if (null == obj)
            return null;

        String jsonStr = JSON.toJSONString(obj);

        return JSONArray.parseArray(jsonStr);
    }

    /**
     * JavaObject 转 JSONObject
     * @param obj
     * @return
     */
    public static JSONObject Object2Json(Object obj) {
        if (null == obj)
            return null;

        return (JSONObject) JSONObject.toJSON(obj);
    }

    /**
     * JavaObject 转 JSONString
     * @param obj
     * @return
     */
    public static String Object2Jstr(Object obj) {
        if (null == obj)
            return null;

        return JSON.toJSONString(obj);
    }
}
