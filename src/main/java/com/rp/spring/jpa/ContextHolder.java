package com.rp.spring.jpa;

import org.springframework.context.ApplicationContext;

import java.util.Collection;
import java.util.Map;

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
		if(appContext == null) {
			System.err.println("appContext尚未注入");
			return null;
		}
		Map<String, T> map = appContext.getBeansOfType(clazz);
		return map == null ? null : map.values();
	}

	public static <T> T getBean(Class<T> clazz) {
		return appContext.getBean(clazz);
	}
}
