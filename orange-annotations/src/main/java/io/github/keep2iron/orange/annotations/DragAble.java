package io.github.keep2iron.orange.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by keep-iron on 17-11-18.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface DragAble {

    int flag() default -1;

}