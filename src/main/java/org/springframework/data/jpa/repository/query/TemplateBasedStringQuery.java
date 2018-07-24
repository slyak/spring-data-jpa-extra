package org.springframework.data.jpa.repository.query;

import org.springframework.expression.ExpressionParser;

/**
 * .
 *
 * @author stormning 2018/5/21
 * @since 1.3.0
 */
public class TemplateBasedStringQuery extends StringQuery {
    private JpaEntityMetadata<?> entityInformation;
    private ExpressionParser parser;

    /**
     * Creates a new {@link StringQuery} from the given JPQL query.
     *
     * @param query must not be {@literal null} or empty.
     */
    TemplateBasedStringQuery(String query) {
        super(query);
    }

    public TemplateBasedStringQuery(String query, JpaEntityMetadata<?> entityInformation, ExpressionParser parser) {
        this(query);
        this.entityInformation = entityInformation;
        this.parser = parser;
    }
}
