package com.slyak.spring.jpa;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.repository.query.AbstractJpaQuery;
import org.springframework.data.jpa.repository.query.JpaParameters;
import org.springframework.data.jpa.repository.query.JpaQueryExecution;
import org.springframework.data.jpa.repository.query.JpaQueryMethod;
import org.springframework.data.repository.query.Parameter;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * 
 * title: CStoredProcedureJpaQuery.java 自定义的存储过程查询
 * 当前版本的jpa的<code>StoredProcedureJpaQuery</code>只支持单个out的查询 如
 * 
 * <pre>
 * &#64;Procedure(name = "JT_000003")
 *	String JT_000003(@Param("RV_ID") String RV_ID);
 * 其中表示调用存储JT_000003入参为RV_ID:String, 出餐out为String(方法的返回类型)
 * </pre>
 * 
 * 现在该查询类解决的就是多个out,返回值则 Map<String, Object> 类型 使用
 * 
 * <pre>
 * &#64;Procedure(name = "JT_000003")
 * &#64;OutParameters({
        &#64;OutParameter(name="out_param1", type=Integer.class),
        &#64;OutParameter(name="inout_param", type=String.class)
    })
 * Map<String, Object> JT_000003(@Param("RV_ID") String RV_ID);
 * 其中表示调用存储JT_000003入参为RV_ID:String, 出餐out为String(方法的返回类型)
 * </pre>
 * 
 * @author rplees
 * @email rplees.i.ly@gmail.com
 * @version 1.0
 * @created Jan 24, 2018 3:04:00 PM
 */
public class CStoredProcedureJpaQuery extends AbstractJpaQuery {
	
	List<COutParameter> outParameters;
	CProcedure procedure;
	private final boolean useNamedParameters;

	/**
	 * Creates a new {@link StoredProcedureJpaQuery}.
	 * 
	 * @param method must not be {@literal null}
	 * @param em must not be {@literal null}
	 */
	public CStoredProcedureJpaQuery(JpaQueryMethod method, EntityManager em) {
		super(method, em);
		Field methodField = ReflectionUtils.findField(JpaQueryMethod.class, "method");
		methodField.setAccessible(true);
		Method sourceMethod = null;
		try {
			sourceMethod = (Method) methodField.get(method);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		this.outParameters = new ArrayList<COutParameter>();
		procedure = AnnotationUtils.findAnnotation(sourceMethod, CProcedure.class);
		Assert.notNull(procedure, "CProcedure must not be null!");
		
		COutParameters ops = AnnotationUtils.findAnnotation(sourceMethod, COutParameters.class);
		if(ops == null) {
			COutParameter op = AnnotationUtils.findAnnotation(sourceMethod, COutParameter.class);
			if(op == null) {
				
			} else {
				outParameters.add(op);
			}
		} else {
			outParameters.addAll(Arrays.asList(ops.value()));
		}
		
		this.useNamedParameters = useNamedParameters(method);

	}
	
	@Override
	protected JpaQueryExecution getExecution() {
		return new CProcedureExecution();
	}
	
	static class CProcedureExecution extends JpaQueryExecution {

		/* 
		 * (non-Javadoc)
		 * @see org.springframework.data.jpa.repository.query.JpaQueryExecution#doExecute(org.springframework.data.jpa.repository.query.AbstractJpaQuery, java.lang.Object[])
		 */
		@Override
		protected Object doExecute(AbstractJpaQuery jpaQuery, Object[] values) {
			Assert.isInstanceOf(CStoredProcedureJpaQuery.class, jpaQuery);
			CStoredProcedureJpaQuery storedProcedureJpaQuery = (CStoredProcedureJpaQuery) jpaQuery;
			StoredProcedureQuery storedProcedure = storedProcedureJpaQuery.createQuery(values);
			storedProcedure.execute();
			return storedProcedureJpaQuery.extractOutputValue(storedProcedure);
		}
	}

	/**
	 * Determine whether to used named parameters for the given query method.
	 * 
	 * @param method
	 *            must not be {@literal null}.
	 * @return
	 */
	private static boolean useNamedParameters(QueryMethod method) {

		for (Parameter parameter : method.getParameters()) {
			if (parameter.isNamedParameter()) {
				return true;
			}
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.jpa.repository.query.AbstractJpaQuery#createQuery(
	 * java.lang.Object[])
	 */
	@Override
	protected StoredProcedureQuery createQuery(Object[] values) {
		return applyHints(doCreateQuery(values), getQueryMethod());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.jpa.repository.query.AbstractJpaQuery#doCreateQuery(
	 * java.lang.Object[])
	 */
	@Override
	protected StoredProcedureQuery doCreateQuery(Object[] values) {
		return createBinder(values).bind(createStoredProcedure());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.jpa.repository.query.AbstractJpaQuery#
	 * doCreateCountQuery(java.lang.Object[])
	 */
	@Override
	protected TypedQuery<Long> doCreateCountQuery(Object[] values) {
		return null;
	}

	/**
	 * Extracts the output value from the given {@link StoredProcedureQuery}.
	 * 
	 * @param storedProcedureQuery
	 *            must not be {@literal null}.
	 * @return
	 */
	Object extractOutputValue(StoredProcedureQuery storedProcedureQuery) {
		Assert.notNull(storedProcedureQuery, "CStoredProcedureQuery must not be null!");
		JpaQueryMethod queryMethod = getQueryMethod();
		Class<?> returnedObjectType = queryMethod.getReturnedObjectType();
		
		if(! hasReturnValue()) {//方法定义为void返回值
			return null;
		}
		
		if(returnedObjectType.isAssignableFrom(Map.class)) {//返回值为Map的处理
			Map<String, Object> retMap = new LinkedHashMap<String, Object>();
			Assert.notEmpty(outParameters, "outParameters在返回值为Map.class的时候不能为空");
			for(int i = 0; i < outParameters.size(); i ++) {
				COutParameter op = outParameters.get(i);
				String name = op.name();
				Object outputParameterValue = null;
				if(useNamedParameters) {
					outputParameterValue = storedProcedureQuery.getOutputParameterValue(name);
				} else {
					JpaParameters parameters = getQueryMethod().getParameters();
					outputParameterValue = storedProcedureQuery.getOutputParameterValue(parameters.getNumberOfParameters() + (i + 1));
				}
				
				retMap.put(name, outputParameterValue);
			}
			
			return retMap;
		} else if(returnedObjectType.isArray()) {
			Assert.notEmpty(outParameters, "outParameters在返回值为Array的时候不能为空");
			List<Object> retList = new ArrayList<Object>();
			for(int i = 0; i < outParameters.size(); i ++) {
				COutParameter op = outParameters.get(i);
				String name = op.name();
				Object outputParameterValue = null;
				if(useNamedParameters) {
					outputParameterValue = storedProcedureQuery.getOutputParameterValue(name);
				} else {
					JpaParameters parameters = getQueryMethod().getParameters();
					outputParameterValue = storedProcedureQuery.getOutputParameterValue(parameters.getNumberOfParameters() + (i + 1));
				}
				
				retList.add(outputParameterValue);
			}
			Object[] retArray = new Object[retList.size()];
			return retList.toArray(retArray );
		} else if(returnedObjectType.isAssignableFrom(List.class)) {
			Assert.notEmpty(outParameters, "outParameters在返回值为List的时候不能为空");
			List<Object> retList = new ArrayList<Object>();
			for(int i = 0; i < outParameters.size(); i ++) {
				COutParameter op = outParameters.get(i);
				String name = op.name();
				Object outputParameterValue = null;
				if(useNamedParameters) {
					outputParameterValue = storedProcedureQuery.getOutputParameterValue(name);
				} else {
					JpaParameters parameters = getQueryMethod().getParameters();
					outputParameterValue = storedProcedureQuery.getOutputParameterValue(parameters.getNumberOfParameters() + (i + 1));
				}
				retList.add(outputParameterValue);
			}
			return retList;
		} else {
			JpaParameters parameters = getQueryMethod().getParameters();
			return storedProcedureQuery.getOutputParameterValue(parameters.getNumberOfParameters() + 1);
		}
	}

	private boolean hasReturnValue() {
		JpaQueryMethod queryMethod = getQueryMethod();
		Class<?> returnedObjectType = queryMethod.getReturnedObjectType();
		
		if(void.class.equals(returnedObjectType) || Void.class.equals(returnedObjectType)) {//方法定义为void返回值
			return false;
		}
		return true;
	}
	/**
	 * Creates a new JPA 2.1 {@link StoredProcedureQuery} from this
	 * {@link StoredProcedureJpaQuery}.
	 * 
	 * @return
	 */
	private StoredProcedureQuery createStoredProcedure() {
		return newAdhocStoredProcedureQuery();
	}

	/**
	 * Creates a new ad-hoc {@link StoredProcedureQuery} from the given
	 * {@link StoredProcedureAttributes}.
	 * 
	 * @return
	 */
	private StoredProcedureQuery newAdhocStoredProcedureQuery() {
		JpaParameters params = getQueryMethod().getParameters();
		String procedureName = procedure.value();
		StoredProcedureQuery procedureQuery = getEntityManager().createStoredProcedureQuery(procedureName);

		for (Parameter param : params) {

			if (!param.isBindable()) {
				continue;
			}

			if (useNamedParameters) {
				procedureQuery.registerStoredProcedureParameter(param.getName(), param.getType(), ParameterMode.IN);
			} else {
				procedureQuery.registerStoredProcedureParameter(param.getIndex() + 1, param.getType(), ParameterMode.IN);
			}
		}

		if (hasReturnValue()) {
			ParameterMode mode = ParameterMode.OUT;
			if (useNamedParameters) {
				for(COutParameter op : outParameters) {
					procedureQuery.registerStoredProcedureParameter(op.name(), op.type(), mode);
				}
			} else {
				int i = 1;
				for(COutParameter op : outParameters) {
					procedureQuery.registerStoredProcedureParameter(params.getNumberOfParameters() + i, op.type(), mode);
					i ++;
				}
			}
		}

		return procedureQuery;
	}
}
