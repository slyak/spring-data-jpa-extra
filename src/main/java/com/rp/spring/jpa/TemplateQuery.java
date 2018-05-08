package com.rp.spring.jpa;

import org.springframework.data.annotation.QueryAnnotation;

import java.lang.annotation.*;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@QueryAnnotation
@Documented
public @interface TemplateQuery {
	String value() default "";
}
