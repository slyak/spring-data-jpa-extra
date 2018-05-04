package com.slyak.spring.jpa;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.Entity;
import javax.persistence.EntityManager;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.AfterAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.OrderUtils;
import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.support.RepositoryProxyPostProcessor;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.util.CollectionUtils;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/9.
 */
public class GenericJpaRepositoryFactory extends JpaRepositoryFactory {
	private final EntityManager entityManager;

	private final PersistenceProvider extractor;

	private Map<Class<?>, List<EntityAssembler>> assemblers = new ConcurrentHashMap<Class<?>, List<EntityAssembler>>();

	/**
	 * Creates a new {@link JpaRepositoryFactory}.
	 *
	 * @param entityManager must not be {@literal null}
	 */
	public GenericJpaRepositoryFactory(EntityManager entityManager) {
		super(entityManager);
		this.entityManager = entityManager;
		this.extractor = PersistenceProvider.fromEntityManager(entityManager);

		final AssemblerInterceptor assemblerInterceptor = new AssemblerInterceptor();
		addRepositoryProxyPostProcessor(new RepositoryProxyPostProcessor() {
			@Override
			public void postProcess(ProxyFactory factory, RepositoryInformation repositoryInformation) {
				factory.addAdvice(assemblerInterceptor);
			}
		});
	}

	@Override
	protected QueryLookupStrategy getQueryLookupStrategy(QueryLookupStrategy.Key key,
			EvaluationContextProvider evaluationContextProvider) {
		return TemplateQueryLookupStrategy.create(entityManager, key, extractor, evaluationContextProvider);
	}

	private List<EntityAssembler> getEntityAssemblers(Class<?> clazz) {
		if (assemblers.isEmpty()) {
			Collection<EntityAssembler> abs = ContextHolder.getBeansOfType(EntityAssembler.class);
			if (abs.isEmpty()) {
				return Collections.emptyList();
			}
			else {
				for (EntityAssembler ab : abs) {
					Class p0 = getGenericParameter0(ab.getClass());
					List<EntityAssembler> ass = this.assemblers.get(p0);
					if (ass == null) {
						ass = new ArrayList<EntityAssembler>();
						assemblers.put(p0, ass);
					}
					ass.add(ab);
				}
				for (List<EntityAssembler> ess : assemblers.values()) {
					Collections.sort(ess, new Comparator<EntityAssembler>() {
						@Override
						public int compare(EntityAssembler o1, EntityAssembler o2) {
							return OrderUtils.getOrder(o2.getClass()) - OrderUtils.getOrder(o1.getClass());
						}
					});
				}
			}
		}
		return assemblers.get(clazz);
	}

	@SuppressWarnings("unchecked")
	private void massemble(Iterable iterable) {
		if (!iterable.iterator().hasNext()) {
			return;
		}

		Object object = iterable.iterator().next();
		if (isEntityObject(object)) {
			List<EntityAssembler> entityAssemblers = getEntityAssemblers(object.getClass());
			if (!CollectionUtils.isEmpty(entityAssemblers)) {
				for (EntityAssembler assembler : entityAssemblers) {
					assembler.massemble(iterable);
				}
			}
		}
	}

	private boolean isEntityObject(Object object) {
		return object != null && AnnotationUtils.findAnnotation(object.getClass(), Entity.class) != null;
	}

	private Class getGenericParameter0(Class clzz) {
		return (Class) ((ParameterizedType) clzz.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	@SuppressWarnings("unchecked")
	public class AssemblerInterceptor implements MethodInterceptor, AfterAdvice {

		@Override
		public Object invoke(MethodInvocation invocation) throws Throwable {
			Object proceed = invocation.proceed();
			if (!"save".equals(invocation.getMethod().getName())) {
				if (proceed != null) {
					//EntityAssembler
					if (proceed instanceof Iterable) {
						massemble((Iterable) proceed);
					}
					else if (proceed instanceof Map) {
						massemble(((Map) proceed).values());
					}
					else if (isEntityObject(proceed)) {
						List<EntityAssembler> entityAssemblers = getEntityAssemblers(proceed.getClass());
						if (!CollectionUtils.isEmpty(entityAssemblers)) {
							for (EntityAssembler assembler : entityAssemblers) {
								assembler.assemble(proceed);
							}
						}
					}
				}
			}
			return proceed;
		}
	}
}
