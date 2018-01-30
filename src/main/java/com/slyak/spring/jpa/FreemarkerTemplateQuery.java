package com.slyak.spring.jpa;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.SQLQuery;
import org.hibernate.jpa.internal.QueryImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.JpaParameters;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.CollectionUtils;

import com.slyak.util.AopTargetUtils;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/9.
 */
public class FreemarkerTemplateQuery extends AbstractJpaQuery {

    private boolean useJpaSpec = false;
    boolean isFistParameterIsMethodQuery = false;
    /**
     * Creates a new {@link AbstractJpaQuery} from the given {@link JpaQueryMethod}.
     *
     * @param method
     * @param em
     */
    public FreemarkerTemplateQuery(JpaQueryMethod method, EntityManager em) {
		this(method, em, false);
	}
    
    public FreemarkerTemplateQuery(JpaQueryMethod method, EntityManager em, boolean isFistParameterIsMethodQuery) {
		super(method, em);
		this.isFistParameterIsMethodQuery = isFistParameterIsMethodQuery;
	}

    @Override
    protected Query doCreateQuery(Object[] values) {
        String nativeQuery = getQuery(values);
        JpaParameters parameters = getQueryMethod().getParameters();
        ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
        String sortedQueryString = QueryUtils
                .applySorting(nativeQuery, accessor.getSort(), QueryUtils.detectAlias(nativeQuery));
        Query query = bind(createJpaQuery(sortedQueryString), values);
        if (parameters.hasPageableParameter()) {
            Pageable pageable = (Pageable) (values[parameters.getPageableIndex()]);
            if (pageable != null) {
                query.setFirstResult(pageable.getOffset());
                query.setMaxResults(pageable.getPageSize());
            }
        }
        return query;
    }

    private String getQuery(Object[] values) {
        return getQueryFromTpl(values);
    }

    private String getQueryFromTpl(Object[] values) {
    		if (isFistParameterIsMethodQuery) {
			if (values == null || values.length < 2) {
				throw new IllegalArgumentException("第一个参数为调用的方法名时参数必须 >= 1");
			}
			String methodName = String.valueOf(values[0]);
			return ContextHolder.getBean(FreemarkerSqlTemplates.class).process(getEntityName(), methodName,
					getParams(values));
		} else {
			return ContextHolder.getBean(FreemarkerSqlTemplates.class).process(getEntityName(), getMethodName(),
					getParams(values));
		}    	
    }

    private Map<String, Object> getParams(Object[] values) {
        JpaParameters parameters = getQueryMethod().getParameters();
        //gen model
        Map<String, Object> params = new HashMap<String, Object>();
        for (int i = 0; i < parameters.getNumberOfParameters(); i++) {
            Object value = values[i];
            Parameter parameter = parameters.getParameter(i);
            if (value != null && canBindParameter(parameter)) {
                if (!QueryBuilder.isValidValue(value)) {
                    continue;
                }
                Class<?> clz = value.getClass();
                if (clz.isPrimitive() || String.class.isAssignableFrom(clz) || Number.class.isAssignableFrom(clz)
                        || clz.isArray() || Collection.class.isAssignableFrom(clz) || clz.isEnum()) {
                    params.put(parameter.getName(), value);
                } else {
                    params = QueryBuilder.toParams(value);
                }
            }
        }
        return params;
    }

    @SuppressWarnings("rawtypes")
    public Query createJpaQuery(String queryString) {
        Class<?> objectType = getQueryMethod().getReturnedObjectType();

        //get original proxy query.
        Query oriProxyQuery;

        //must be hibernate QueryImpl
        QueryImpl query;

        if (useJpaSpec && getQueryMethod().isQueryForEntity()) {
            oriProxyQuery = getEntityManager().createNativeQuery(queryString, objectType);

//            QueryImpl query = AopTargetUtils.getTarget(oriProxyQuery);
        } else {
            oriProxyQuery = getEntityManager().createNativeQuery(queryString);

			query = AopTargetUtils.getTarget(oriProxyQuery);
			//find generic type
			ClassTypeInformation<?> ctif = ClassTypeInformation.from(objectType);
			if (ctif != null) {
				TypeInformation<?> actualType = ctif.getActualType();
				if (actualType == null){
				    actualType = ctif.getRawTypeInformation();
	            }
				Class<?> genericType = actualType.getType();
				if (genericType != null && genericType != Void.class) {
					QueryBuilder.transform(query.getHibernateQuery(), genericType);
				}
			}
			
			
		}
        //return the original proxy query, for a series of JPA actions, e.g.:close em.
        return oriProxyQuery;
    }

    private String getEntityName() {
    		//解决Java类名与EntityName不一致导致找不到模板文件
    		return getQueryMethod().getEntityInformation().getEntityName();
        //return getQueryMethod().getEntityInformation().getJavaType().getSimpleName();
    }

    private String getMethodName() {
        return getQueryMethod().getName();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected TypedQuery<Long> doCreateCountQuery(Object[] values) {
        TypedQuery query = (TypedQuery) getEntityManager()
                .createNativeQuery(QueryBuilder.toCountQuery(getQuery(values)));
        bind(query, values);
        return query;
    }
    
    @SuppressWarnings({ "rawtypes"})
    public Query bind(Query query, Object[] values) {
    	//get proxy target if exist.
        //must be hibernate QueryImpl
        QueryImpl targetQuery = AopTargetUtils.getTarget(query);

		SQLQuery sqlQuery = (SQLQuery) targetQuery.getHibernateQuery();
        Map<String, Object> params = getParams(values);
        if (!CollectionUtils.isEmpty(params)) {
            QueryBuilder.setParams(sqlQuery, params);
        }
        return query;
    }

    protected boolean canBindParameter(Parameter parameter) {
        return parameter.isBindable();
    }
}
