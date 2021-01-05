package com.rp.spring.jpa;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.lang.Nullable;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/9.
 */
public class TemplateQueryLookupStrategy implements QueryLookupStrategy {

	private final EntityManager entityManager;

	private QueryLookupStrategy jpaQueryLookupStrategy;

//	private QueryExtractor extractor;
	private JpaQueryMethodFactory queryMethodFactory;

	public TemplateQueryLookupStrategy(EntityManager entityManager, JpaQueryMethodFactory queryMethodFactory,
			@Nullable Key key, QueryMethodEvaluationContextProvider evaluationContextProvider, EscapeCharacter escape) {
		this.jpaQueryLookupStrategy = JpaQueryLookupStrategy.create(entityManager, queryMethodFactory,key, evaluationContextProvider, escape);
//		this.extractor = extractor;
		this.queryMethodFactory = queryMethodFactory;
		this.entityManager = entityManager;
	}

	public static QueryLookupStrategy create(EntityManager em, JpaQueryMethodFactory queryMethodFactory,
			@Nullable Key key, QueryMethodEvaluationContextProvider evaluationContextProvider, EscapeCharacter escape) {
		return new TemplateQueryLookupStrategy(em, queryMethodFactory,key, evaluationContextProvider, escape);
	}
	
//	public static QueryLookupStrategy create(EntityManager entityManager, Key key, QueryExtractor extractor,
//			QueryMethodEvaluationContextProvider evaluationContextProvider, EscapeCharacter ec) {
//		return new TemplateQueryLookupStrategy(entityManager, key, extractor, evaluationContextProvider, ec);
//	}

	@Override
	public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
			NamedQueries namedQueries) {
		JpaQueryMethod jpaQueryMethod = queryMethodFactory.build(method, metadata, factory);
		
		if (method.getAnnotation(TemplateQuery.class) != null) {
			return new FreemarkerTemplateQuery(jpaQueryMethod, entityManager);
		} else if(method.getAnnotation(FistParameterIsMethodQuery.class) != null) {
			return new FreemarkerTemplateQuery(jpaQueryMethod, entityManager, true);
		}if (method.getAnnotation(CProcedure.class) != null) {
			throw new RuntimeException("暂不支持CProcedure注解实现");
//			return new CStoredProcedureJpaQuery(jpaQueryMethod, entityManager);
		} else {
			return jpaQueryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
		}
	}
}
