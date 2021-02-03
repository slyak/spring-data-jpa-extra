//package org.springframework.data.jpa.repository.query;
//
//import org.springframework.data.repository.query.EvaluationContextProvider;
//import org.springframework.data.util.StreamUtils;
//import org.springframework.expression.ExpressionParser;
//import org.springframework.util.Assert;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Objects;
//
///**
// * .
// *
// * @author stormning 2018/5/21
// * @since 1.3.0
// */
//public class TemplateQueryParameterBinderFactory {
//
//    public static ParameterBinder createQueryAwareBinder(JpaParameters parameters, DeclaredQuery query,
//                                                         ExpressionParser parser, EvaluationContextProvider evaluationContextProvider) {
//
//        Assert.notNull(parameters, "JpaParameters must not be null!");
//        Assert.notNull(query, "StringQuery must not be null!");
//        Assert.notNull(parser, "ExpressionParser must not be null!");
//        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");
//
//        List<StringQuery.ParameterBinding> bindings = query.getParameterBindings();
//        QueryParameterSetterFactory expressionSetterFactory = TemplateBasedQueryParameterSetterFactory.parsingTemplate(parser,
//                evaluationContextProvider, parameters);
//        QueryParameterSetterFactory basicSetterFactory = TemplateBasedQueryParameterSetterFactory.basic(parameters);
//
//        return new ParameterBinder(parameters, createSetters(bindings, query, expressionSetterFactory, basicSetterFactory),
//                !query.usesPaging());
//    }
//
//
//    private static Iterable<QueryParameterSetter> createSetters(List<StringQuery.ParameterBinding> parameterBindings,
//                                                                QueryParameterSetterFactory... factories) {
//        return createSetters(parameterBindings, EmptyDeclaredQuery.EMPTY_QUERY, factories);
//    }
//
//    private static Iterable<QueryParameterSetter> createSetters(List<StringQuery.ParameterBinding> parameterBindings,
//                                                                DeclaredQuery declaredQuery, QueryParameterSetterFactory... strategies) {
//
//        return parameterBindings.stream() //
//                .map(it -> createQueryParameterSetter(it, strategies, declaredQuery)) //
//                .collect(StreamUtils.toUnmodifiableList());
//    }
//
//    private static QueryParameterSetter createQueryParameterSetter(StringQuery.ParameterBinding binding,
//                                                                   QueryParameterSetterFactory[] strategies, DeclaredQuery declaredQuery) {
//
//        return Arrays.stream(strategies)//
//                .map(it -> it.create(binding, declaredQuery))//
//                .filter(Objects::nonNull)//
//                .findFirst()//
//                .orElse(QueryParameterSetter.NOOP);
//    }
//}
