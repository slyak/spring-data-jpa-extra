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
@Target({ ElementType.TYPE })
@QueryAnnotation
@Documented
public @interface TemplateQueryObject {
	String value() default "";
}
