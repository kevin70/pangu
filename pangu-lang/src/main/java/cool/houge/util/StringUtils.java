package cool.houge.util;

/**
 * 字符串工具类.
 *
 * @author ZY (kzou227@qq.com)
 */
public final class StringUtils {

    /**
     * 字符串是否为<code>null</code>或空字符串.
     * @param s 字符串
     * @return true/false
     */
    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * 字符串是否非<code>null</code>或空字符串.
     * @param s 字符串
     * @return true/false
     */
    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}
