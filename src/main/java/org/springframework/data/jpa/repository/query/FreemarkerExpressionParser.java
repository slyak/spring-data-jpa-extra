package org.springframework.data.jpa.repository.query;

import org.springframework.expression.Expression;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateAwareExpressionParser;
import org.springframework.lang.Nullable;

/**
 * .
 *
 * @author stormning 2018/5/21
 * @since 1.3.0
 */
public class FreemarkerExpressionParser extends TemplateAwareExpressionParser {
    @Override
    protected Expression doParseExpression(String s, @Nullable ParserContext parserContext) throws ParseException {
        return null;
    }
}
