package org.yaps.petstore.util.service;

import javax.persistence.EntityManager;

/**
 * Class defines base service functions.
 * 
 * @param <T>
 *            - type of the entity
 */
public abstract class AbstractFacade<T> implements Facade<T> {
	/**
	 * Entity class
	 */
	private final Class<T> entityClass;

	/**
	 * Constructor
	 * 
	 * @param entityClass
	 *            - entity class
	 */
	public AbstractFacade(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	/**
	 * Get entity manager
	 * 
	 * @return entity manager
	 */
	protected abstract EntityManager getEntityManager();

	/**
	 * Insert new entity on <T> type in database.
	 * 
	 * @param entity
	 *            - entity to create
	 * @return created entity
	 */
	@Override
	public T create(T entity) {
		getEntityManager().persist(entity);
		return entity;
	}

	/**
	 * Update entity on <T> type in database.
	 * 
	 * @param entity
	 *            - entity to update
	 * @return updated entity
	 */
	@Override
	public T update(T entity) {
		return getEntityManager().merge(entity);
	}

	/**
	 * Remove entity on <T> type in database.
	 * 
	 * @param entity
	 *            - entity to remove
	 */
	@Override
	public void delete(T entity) {
		getEntityManager().remove(getEntityManager().merge(entity));
	}

	/**
	 * Find entity by primary key
	 * 
	 * @param id
	 *            - primary key object
	 * @return found entity
	 */
	@Override
	public T find(Object id) {
		return getEntityManager().find(entityClass, id);
	}
}
