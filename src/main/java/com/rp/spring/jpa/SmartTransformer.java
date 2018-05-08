package com.rp.spring.jpa;

import org.hibernate.transform.BasicTransformerAdapter;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.List;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/9/1.
 */
public class SmartTransformer extends BasicTransformerAdapter {

	private static DefaultConversionService conversionService = new DefaultConversionService();

	private final Class clazz;

	public SmartTransformer(Class clazz) {
		this.clazz = clazz;
	}

	@Override
	public Object transformTuple(Object[] tuple, String[] aliases) {
		if (tuple != null && tuple.length > 0) {
			return conversionService.convert(tuple[0], clazz);
		}
		return null;
	}

	@Override
	public List transformList(List list) {
		return super.transformList(list);
	}
}
