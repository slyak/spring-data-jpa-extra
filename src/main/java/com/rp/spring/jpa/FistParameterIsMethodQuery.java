package com.rp.spring.jpa;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.data.annotation.QueryAnnotation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@QueryAnnotation
@Documented
/**
 * 
 * title: FistParameterIsMethodQuery.java 
 * 第一个参数就是方法名查询,此注解会忽略Method,采用第一个入参作为方法名
 * 适合执行方法动态化
 *
 * @author rplees
 * @email rplees.i.ly@gmail.com
 * @version 1.0  
 * @created Jan 18, 2018 3:17:03 PM
 */
public @interface FistParameterIsMethodQuery {
	String value() default "";
}
