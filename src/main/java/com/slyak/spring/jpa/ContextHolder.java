package com.slyak.spring.jpa;

import org.springframework.context.ApplicationContext;

import java.util.Collection;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 16/3/15.
 */
public class ContextHolder {
	public static ApplicationContext appContext;

	public static <T> Collection<T> getBeansOfType(Class<T> clazz) {
		return appContext.getBeansOfType(clazz).values();
	}

	public static <T> T getBean(Class<T> clazz) {
		return appContext.getBean(clazz);
	}
}
