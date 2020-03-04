package com.rp.spring.jpa;

import java.lang.reflect.Method;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;

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

	private QueryExtractor extractor;

	public TemplateQueryLookupStrategy(EntityManager entityManager, Key key, QueryExtractor extractor,
			QueryMethodEvaluationContextProvider evaluationContextProvider, EscapeCharacter ec) {
		this.jpaQueryLookupStrategy = JpaQueryLookupStrategy.create(entityManager, key, extractor, evaluationContextProvider, ec);
		this.extractor = extractor;
		this.entityManager = entityManager;
	}

	public static QueryLookupStrategy create(EntityManager entityManager, Key key, QueryExtractor extractor,
			QueryMethodEvaluationContextProvider evaluationContextProvider, EscapeCharacter ec) {
		return new TemplateQueryLookupStrategy(entityManager, key, extractor, evaluationContextProvider, ec);
	}

	@Override
	public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
			NamedQueries namedQueries) {
		if (method.getAnnotation(TemplateQuery.class) != null) {
			return new FreemarkerTemplateQuery(new JpaQueryMethod(method, metadata, factory, extractor), entityManager);
		} else if(method.getAnnotation(FistParameterIsMethodQuery.class) != null) {
			return new FreemarkerTemplateQuery(new JpaQueryMethod(method, metadata, factory, extractor), entityManager, true);
		}if (method.getAnnotation(CProcedure.class) != null) {
			return new CStoredProcedureJpaQuery(new JpaQueryMethod(method, metadata, factory, extractor), entityManager);
		} else {
			return jpaQueryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
		}
	}
}
