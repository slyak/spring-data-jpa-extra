package com.rp.spring.jpa;

/**
 * .
 *
 * @author stormning on 16/6/5.
 */
public interface QueryTemplateContext {
	QueryTemplate lookup(String namedQueryName);
}
