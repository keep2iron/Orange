package io.github.keep2iron.orange.annotations.bind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/09 12:37
 * <p>
 * bin
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface BindOnRefresh {

    /**
     * onRefreshListener的class对象
     */
    Class<?> listener() default void.class;

    /**
     * refresh layout的class对象
     */
    Class<?> refreshLayout() default void.class;

    /**
     * 设置Listener的方法名
     */
    String setRefreshMethod() default "setOnRefreshListener";
}