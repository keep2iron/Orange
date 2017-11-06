package io.github.keep2iron.orange.annotations;

import android.support.annotation.LayoutRes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/04 22:57
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RecyclerHolder {
    Class<?> type();

    @LayoutRes int[] items();

    @LayoutRes int header() default -1;

    boolean isUseDataBinding() default false;
}
