package io.github.keep2iron.orange.annotations.bind;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/06 18:48
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface BindOnItemClick {
}
