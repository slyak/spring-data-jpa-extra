package com.slyak.spring.jpa;

import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.provider.QueryExtractor;
import org.springframework.data.jpa.repository.query.DefaultJpaQueryMethodFactory;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.query.JpaQueryLookupStrategy;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;

import javax.persistence.EntityManager;
import java.lang.reflect.Method;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/9.
 */
public class TemplateQueryLookupStrategy implements QueryLookupStrategy {

    private final EntityManager entityManager;
    private final DefaultJpaQueryMethodFactory queryMethodFactory;

    private QueryLookupStrategy jpaQueryLookupStrategy;

    private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

    private QueryExtractor extractor;


    public TemplateQueryLookupStrategy(EntityManager entityManager, Key key, QueryExtractor extractor,
                                       QueryMethodEvaluationContextProvider evaluationContextProvider) {
        this.extractor = PersistenceProvider.fromEntityManager(entityManager);
        this.queryMethodFactory = new DefaultJpaQueryMethodFactory(extractor);
        this.jpaQueryLookupStrategy = JpaQueryLookupStrategy.create(entityManager, queryMethodFactory, key, evaluationContextProvider, escapeCharacter);
        this.extractor = extractor;
        this.entityManager = entityManager;
    }

    public static QueryLookupStrategy create(EntityManager entityManager, Key key, QueryExtractor extractor,
                                             QueryMethodEvaluationContextProvider evaluationContextProvider) {
        return new TemplateQueryLookupStrategy(entityManager, key, extractor, evaluationContextProvider);
    }

    @Override
    public RepositoryQuery resolveQuery(Method method, RepositoryMetadata metadata, ProjectionFactory factory,
                                        NamedQueries namedQueries) {
        if (method.getAnnotation(TemplateQuery.class) == null) {
            return jpaQueryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
        } else {
            return new FreemarkerTemplateQuery(queryMethodFactory.build(method, metadata, factory), entityManager);
        }
    }
}
