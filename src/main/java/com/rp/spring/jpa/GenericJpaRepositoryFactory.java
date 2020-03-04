package com.rp.spring.jpa;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.provider.PersistenceProvider;
import org.springframework.data.jpa.repository.query.EscapeCharacter;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.lang.Nullable;

/**
 * 
 * title: GenericJpaRepositoryFactory.java 
 * description
 *
 * @author rplees
 * @email rplees.i.ly@gmail.com
 * @version 1.0  
 * @created May 4, 2018 3:04:31 PM
 */
public class GenericJpaRepositoryFactory extends JpaRepositoryFactory {
	private final EntityManager entityManager;

	private final PersistenceProvider extractor;

	private EscapeCharacter escapeCharacter = EscapeCharacter.DEFAULT;

	/**
	 * Creates a new {@link JpaRepositoryFactory}.
	 *
	 * @param entityManager must not be {@literal null}
	 */
	public GenericJpaRepositoryFactory(EntityManager entityManager) {
		super(entityManager);
		this.entityManager = entityManager;
		this.extractor = PersistenceProvider.fromEntityManager(entityManager);
	}
	
	@Override
	protected Optional<QueryLookupStrategy> getQueryLookupStrategy(@Nullable Key key,
			QueryMethodEvaluationContextProvider evaluationContextProvider) {
		return Optional.of(TemplateQueryLookupStrategy.create(entityManager, key, extractor, evaluationContextProvider, escapeCharacter));
	}

	@Override
	public void setEscapeCharacter(EscapeCharacter escapeCharacter) {
		super.setEscapeCharacter(escapeCharacter);
		this.escapeCharacter  = escapeCharacter;
	}
}
