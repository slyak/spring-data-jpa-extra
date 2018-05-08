package com.rp.util;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 16/3/17.
 */
public class AopTargetUtils {

	/**
	 * 获取 目标对象
	 *
	 * @param proxy 代理对象
	 * @return 目标对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getTarget(Object proxy) {
		if (Proxy.isProxyClass(proxy.getClass())) {
			return getJdkDynamicProxyTargetObject(proxy);
		}
		else if (ClassUtils.isCglibProxy(proxy)) {
			return getCglibProxyTargetObject(proxy);
		}
		else {
			return (T) proxy;
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getCglibProxyTargetObject(Object proxy) {
		try {
			Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0");
			h.setAccessible(true);
			Object dynamicAdvisedInterceptor = h.get(proxy);
			Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised");
			advised.setAccessible(true);
			return (T) (((AdvisedSupport) advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget());
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T getJdkDynamicProxyTargetObject(Object proxy) {
		try {
			Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
			h.setAccessible(true);
			Object proxy_ = h.get(proxy);
			Field f = proxy_.getClass().getDeclaredField("target");
			f.setAccessible(true);
			return (T) f.get(proxy_);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
