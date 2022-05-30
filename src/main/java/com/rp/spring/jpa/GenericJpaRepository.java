package com.rp.spring.jpa;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;

/**
 * 
 * title: GenericJpaRepository.java 
 * description
 *
 * @author rplees
 * @email rplees.i.ly@gmail.com
 * @version 1.0  
 * @created May 4, 2018 3:25:51 PM
 */
@NoRepositoryBean
public interface GenericJpaRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, QuerydslPredicateExecutor<T>, JpaSpecificationExecutor<T> {
	final int STATUS_NORMAL = 1;
	final int STATUS_DISABLE = -1;
	
	/**
	 * 条件查询返回最大数量的集合
	 * @param predicate
	 * @param maxResult
	 * @return
	 */
	List<T> findAll(Predicate predicate, int maxResult);
	
	List<T> findAll(Predicate predicate, int maxResult, OrderSpecifier<?>... orders);
	
	List<T> findAll(Predicate predicate);
	
	List<T> findAll(Predicate... predicate);
	
	List<T> findAll(Predicate predicate, Sort sort);

	List<T> findAll(Predicate predicate, OrderSpecifier<?>... orders);
	
	List<T> findAll(OrderSpecifier<?>... orders);
	
	long count(Predicate... predicate);
	
	<K> Page<K> findAll(JPQLQuery<K> jpqlQuery, Pageable pageable, OrderSpecifier<?>... sorts);
	
	<K> Page<K> findAll(JPQLQuery<K> jpqlQuery, Pageable pageable);
	
	<K> Page<K> findAll(Predicate predicate, Pageable pageable, OrderSpecifier<?>... sorts);
	
	T findOneIfMutil(Predicate... predicate);
	
	/**
	 * 设置实体.status的状态为 启用/正常 状态
	 */
	@Transactional
	default void setStatusEnable(ID id) {
		setStatusValue(id, STATUS_NORMAL);
	}
	
	/**
	 * 设置实体.status的状态为 禁用 状态
	 */
	@Transactional
	default void setStatusDisable(ID id) {
		setStatusValue(id, STATUS_DISABLE);
	}
	
	@Transactional
	void setStatusValue(ID id, Integer status);
	
	JpaEntityInformation<T, ID> jpaEntityInformation();
	
	EntityManager em();
	
	JPAQueryFactory getJPAQueryFactory();
	
	JPAUpdateClause update(EntityPath<?> path);
}

