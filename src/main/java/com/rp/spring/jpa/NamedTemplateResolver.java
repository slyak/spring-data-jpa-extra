package com.rp.spring.jpa;

import org.springframework.core.io.Resource;

import java.util.Iterator;

/**
 * .
 *
 * @author stormning on 2016/12/17.
 */
public interface NamedTemplateResolver {
	Iterator<Void> doInTemplateResource(Resource resource, final NamedTemplateCallback callback) throws Exception;
}
