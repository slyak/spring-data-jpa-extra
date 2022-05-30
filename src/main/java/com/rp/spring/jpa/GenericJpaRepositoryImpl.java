package com.rp.spring.jpa;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

/**
 * .
 * <p/>
 *
 * @author <a href="mailto:stormning@163.com">stormning</a>
 * @version V1.0, 2015/8/7
 */
@SuppressWarnings("deprecation")
public class GenericJpaRepositoryImpl<T, ID extends Serializable> extends QuerydslJpaRepository<T, ID>
		implements GenericJpaRepository<T, ID>, Serializable {
	
	/** 描述 */
	private static final long serialVersionUID = -2244136368552010216L;
	private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;
	EntityManager em;
	JpaEntityInformation<T, ID> eif;
	final EntityPath<T> path;
	final PathBuilder<T> builder;
	final Querydsl querydsl;

	PropertyDescriptor statusDescriptor;

	public GenericJpaRepositoryImpl(JpaEntityInformation<T, ID> eif, EntityManager em) {
		this(eif, em, DEFAULT_ENTITY_PATH_RESOLVER);
	}

	public GenericJpaRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager,
			EntityPathResolver resolver) {
		super(entityInformation, entityManager);

		this.em = entityManager;
		this.eif = entityInformation;
		this.path = resolver.createPath(entityInformation.getJavaType());
		this.builder = new PathBuilder<T>(path.getType(), path.getMetadata());
		this.querydsl = new Querydsl(entityManager, builder);

		this.statusDescriptor = findFieldPropertyDescriptor(eif.getJavaType(), "status");
	}
	
	@Override
	public <S extends T> S save(S entity) {
		if (eif.isNew(entity)) {
			em.persist(entity);
			return entity;
		} else {
//			if(entity.getClass().isAnnotationPresent(DynamicUpdate.class)) {
//				BeanUtils.copyProperties(source, target, ignoreProperties);
//			}
			return em.merge(entity);
		}
	}

	/**
	 * 条件查询返回最大数量的集合
	 * 
	 * @param predicate
	 * @param maxResult
	 * @return
	 */
	@Override
	public List<T> findAll(Predicate predicate, int maxResult) {
		return createQuery(predicate).select(path).limit(maxResult).fetch();
	}
	
	@Override
	public List<T> findAll(Predicate predicate, int maxResult, OrderSpecifier<?>... orders) {
		return createQuery(predicate).select(path).limit(maxResult).orderBy(orders).fetch();
	}
	
	@Override
	public List<T> findAll(Predicate predicate) {
		return createQuery(predicate).select(path).fetch();
	}
	
	@Override
	public List<T> findAll(Predicate... predicate) {
		return createQuery(predicate).select(path).fetch();
	}
	
	@Override
	public long count(Predicate... predicate) {
		return createQuery(predicate).select(path).fetchCount();
	}

	@Override
	public T findOneIfMutil(Predicate... predicate) {
		return createQuery(predicate).select(path).fetchFirst();
	}

	@Override
	public <K> Page<K> findAll(JPQLQuery<K> jpqlQuery, Pageable pageable, OrderSpecifier<?>... sorts) {
		Assert.notNull(pageable, "Pageable must not be null!");
		final JPQLQuery<?> countQuery = jpqlQuery;
		JPQLQuery<K> query = querydsl.applyPagination(pageable, jpqlQuery).orderBy(sorts);
		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchCount);
	}
	
	@Override
	public <K> Page<K> findAll(JPQLQuery<K> jpqlQuery, Pageable pageable) {
		Assert.notNull(pageable, "Pageable must not be null!");
		final JPQLQuery<?> countQuery = jpqlQuery;
		JPQLQuery<K> query = querydsl.applyPagination(pageable, jpqlQuery);
		return PageableExecutionUtils.getPage(query.fetch(), pageable, countQuery::fetchCount);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public <K> Page<K> findAll(Predicate predicate, Pageable pageable, OrderSpecifier<?>... sorts) {
		JPQLQuery jpqlQuery = createQuery(predicate).select(path).orderBy(sorts);
		return findAll(jpqlQuery, pageable);
	}

	@Transactional
	@Override
	public void setStatusValue(ID id, Integer status) {
		if (this.statusDescriptor == null) {
			throw new IllegalArgumentException(String.format("实体%s没有status字段.", this.eif.getEntityName()));
		}

		Assert.notNull(id, "主键不能为空!");
		Optional<T> optional = findById(id);
		Assert.isTrue(optional.isPresent(), "找不到数据[" + id + "]");
		
		T t = optional.get();
		Integer dbValue = (Integer) ReflectionUtils.invokeMethod(statusDescriptor.getReadMethod(), t);
		if(com.rp.util.NumberUtils.equals(dbValue, status)) {
			//String.format("已是%s状态",status == STATUS_NORMAL ? "启用" : "禁用" )
			return;
		}
		
//		Assert.isTrue(com.rp.util.NumberUtils.notEquals(dbValue, status), String.format("已是%s状态",status == STATUS_NORMAL ? "启用" : "禁用" ));
		ReflectionUtils.invokeMethod(statusDescriptor.getWriteMethod(), t, status);
		saveAndFlush(t);
	}

	@SuppressWarnings("rawtypes")
	private PropertyDescriptor findFieldPropertyDescriptor(Class target, String propertyName) {
		PropertyDescriptor[] propertyDescriptors = org.springframework.beans.BeanUtils.getPropertyDescriptors(target);
		for (PropertyDescriptor pd : propertyDescriptors) {
			if (pd.getName().equals(propertyName)) {
				return pd;
			}
		}
		return null;
	}

	@Override
	public JpaEntityInformation<T, ID> jpaEntityInformation() {
		return eif;
	}

	@Override
	public EntityManager em() {
		return em;
	}
	
	@Override
	public JPAQueryFactory getJPAQueryFactory() {
		return new JPAQueryFactory(em);
	}
	
	@Override
	public JPAUpdateClause update(EntityPath<?> path) {
		return getJPAQueryFactory().update(path);
	}
}