package org.springframework.data.jpa.repository.query;

import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.util.StreamUtils;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.springframework.data.jpa.repository.query.QueryParameterSetter.ErrorHandling.LENIENT;

/**
 * .
 *
 * @author stormning 2018/5/21
 * @since 1.3.0
 */
public class AbstractTemplateBasedJpaQuery extends AbstractJpaQuery {

    private final DeclaredQuery query;
    private final DeclaredQuery countQuery;
    private final EvaluationContextProvider evaluationContextProvider;
    private final ExpressionParser parser;

    /**
     * Creates a new {@link AbstractJpaQuery} from the given {@link JpaQueryMethod}.
     *
     * @param method
     * @param em
     */
    public AbstractTemplateBasedJpaQuery(JpaQueryMethod method, EntityManager em, String queryString,
                                         EvaluationContextProvider evaluationContextProvider, ExpressionParser parser) {

        super(method, em);
        Assert.hasText(queryString, "Query string must not be null or empty!");
        Assert.notNull(evaluationContextProvider, "ExpressionEvaluationContextProvider must not be null!");
        Assert.notNull(parser, "Parser must not be null or empty!");

        this.evaluationContextProvider = evaluationContextProvider;
        this.query = new TemplateBasedStringQuery(queryString, method.getEntityInformation(), parser);
        this.countQuery = query.deriveCountQuery(method.getCountQuery(), method.getCountQueryProjection());

        this.parser = parser;

        Assert.isTrue(method.isNativeQuery() || !query.usesJdbcStyleParameters(),
                "JDBC style parameters (?) are not supported for JPA queries.");
    }

    @Override
    protected Query doCreateQuery(Object[] values) {
        ParameterAccessor accessor = new ParametersParameterAccessor(getQueryMethod().getParameters(), values);
        String sortedQueryString = QueryUtils.applySorting(query.getQueryString(), accessor.getSort(), query.getAlias());

        Query query = createJpaQuery(sortedQueryString);

        // it is ok to reuse the binding contained in the ParameterBinder although we create a new query String because the
        // parameters in the query do not change.
        return parameterBinder.get().bindAndPrepare(query, values);
    }

    @Override
    protected ParameterBinder createBinder() {
        return TemplateQueryParameterBinderFactory.createQueryAwareBinder(getQueryMethod().getParameters(), query, parser,
                evaluationContextProvider);
    }

    @Override
    protected Query doCreateCountQuery(Object[] values) {

        String queryString = countQuery.getQueryString();
        EntityManager em = getEntityManager();

        Query query = getQueryMethod().isNativeQuery() //
                ? em.createNativeQuery(queryString) //
                : em.createQuery(queryString, Long.class);

        return parameterBinder.get().bind(query, values, LENIENT);
    }

    /**
     * Creates an appropriate JPA query from an {@link EntityManager} according to the current {@link AbstractJpaQuery}
     * type.
     */
    protected Query createJpaQuery(String queryString) {

        EntityManager em = getEntityManager();

        if (this.query.hasConstructorExpression() || this.query.isDefaultProjection()) {
            return em.createQuery(queryString);
        }

        return getTypeToRead() //
                .<Query> map(it -> em.createQuery(queryString, it)) //
                .orElseGet(() -> em.createQuery(queryString));
    }
}
