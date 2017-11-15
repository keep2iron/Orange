package io.github.keep2iron.orange.util;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/15 14:17
 */
class StringUtils {
    public static boolean isNotEmpty(CharSequence info) {
        return info != null && !info.toString().trim().equals("");
    }
}
