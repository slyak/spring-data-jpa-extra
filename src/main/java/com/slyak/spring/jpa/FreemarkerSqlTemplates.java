package com.slyak.spring.jpa;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ClassUtils;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;

/**
 * TODO use exist configuration.
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/10.
 */
public class FreemarkerSqlTemplates implements ResourceLoaderAware, InitializingBean {


	private static Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

	private static StringTemplateLoader sqlTemplateLoader = new StringTemplateLoader();

	static {
		cfg.setTemplateLoader(sqlTemplateLoader);
	}

	protected final Log logger = LogFactory.getLog(getClass());

	private String encoding = "UTF-8";

	private Map<String, Long> lastModifiedCache = new ConcurrentHashMap<String, Long>();

	private Map<String, Resource> sqlResources = new ConcurrentHashMap<String, Resource>();

	private String templateLocationDir = "sqls";
	
//	private String templateLocation = "classpath:/sqls";

	private String templateBasePackage = "**";

	private ResourceLoader resourceLoader;

	private String suffix = ".xml";

	private Map<String, NamedTemplateResolver> suffixResolvers = new HashMap<String, NamedTemplateResolver>();

	{
		suffixResolvers.put(".sftl", new SftlNamedTemplateResolver());
	}

	public Configuration getConfiguration() {
		return cfg;
	}
	
	public String process(String entityName, String methodName, Map<String, Object> model) {
		reloadIfPossible(entityName);
		try {
			StringWriter writer = new StringWriter();
			cfg.getTemplate(getTemplateKey(entityName, methodName), encoding).process(model, writer);
			return writer.toString();
		}
		catch (Exception e) {
			logger.error("process template error. Entity name: " + entityName + " methodName:" + methodName, e);
			return StringUtils.EMPTY;
		}
	}

	private String getTemplateKey(String entityName, String methodName) {
		return entityName + ":" + methodName;
	}

	public void reloadIfPossible(final String entityName) {
		try {
			Long lastModified = lastModifiedCache.get(entityName);
			Resource resource = sqlResources.get(entityName);
			long newLastModified = resource.lastModified();
			if (lastModified == null || newLastModified != lastModified) {
				Iterator<Void> iterator = suffixResolvers.get(suffix)
						.doInTemplateResource(resource, new NamedTemplateCallback() {
							@Override
							public void process(String templateName, String content) {
								sqlTemplateLoader.putTemplate(getTemplateKey(entityName, templateName), content);
							}
						});
				while (iterator.hasNext()) {
					iterator.next();
				}
				lastModifiedCache.put(entityName, newLastModified);
			}
		}
		catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
		XmlNamedTemplateResolver xmlNamedTemplateResolver = new XmlNamedTemplateResolver(resourceLoader);
		xmlNamedTemplateResolver.setEncoding(encoding);
		this.suffixResolvers.put(".xml", xmlNamedTemplateResolver);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Collection<EntityManager> em = ContextHolder.getBeansOfType(EntityManager.class);
		Set<String> names = new HashSet<String>();
		for(EntityManager e : em) {
			Set<EntityType<?>> entities = e.getMetamodel().getEntities();
			for (EntityType<?> entity : entities) {
				names.add(entity.getName());
			}
		}
		String[] DEFAULT_SEARCH_LOCATIONS = { "classpath:/", "classpath:/config/", "file:./", "file:./config/" };
		
		String suffixPattern = "/**/*" + suffix;

		if (!names.isEmpty()) {
			for(String prefix : DEFAULT_SEARCH_LOCATIONS) {
				String pattern;
				if (StringUtils.isNotBlank(templateBasePackage)) {
					pattern = prefix + ClassUtils.convertClassNameToResourcePath(templateBasePackage) + suffixPattern;
				} else {
					pattern = prefix + templateLocationDir + suffixPattern;
				}
				
				logger.info(String.format("开始扫描目录 %s", pattern));
				try {
					PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(resourceLoader);
					Resource[] resources = resourcePatternResolver.getResources(pattern);
					logger.info(String.format("扫描到文件数量 %s", resources.length));
					for (Resource resource : resources) {
						String resourceName = resource.getFilename().replace(suffix, "");
						if (names.contains(resourceName)) {
							if(sqlResources.containsKey(resourceName)) {
								logger.info(String.format("Override file %s from %s", resourceName, prefix));
							}
							sqlResources.put(resourceName, resource);
						}
					}
				} catch(Exception  e) {
					logger.info(String.format("找不到配置文件 %s", pattern));
				}
				
			}
		}
	}

//	public void setTemplateLocation(String templateLocation) {
//		this.templateLocation = templateLocation;
//	}

	public void setTemplateBasePackage(String templateBasePackage) {
		this.templateBasePackage = templateBasePackage;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

}
