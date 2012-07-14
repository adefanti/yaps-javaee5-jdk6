package org.yaps.petstore.util.service;

/**
 * Class defines base service functions.
 * 
 * @param <T>
 *            - type of the entity
 */
public interface Facade<T> {

	/**
	 * Insert new entity on <T> type in database.
	 * 
	 * @param entity
	 *            - entity to creatz
	 * @return created entity
	 */
	T create(T entity);

	/**
	 * Update entity on <T> type in database.
	 * 
	 * @param entity
	 *            - entity to update
	 * @return updated entity
	 */
	T update(T entity);

	/**
	 * Remove entity on <T> type in database.
	 * 
	 * @param entity
	 *            - entity to remove
	 */
	void delete(T entity);

	/**
	 * Find entity by primary key
	 * 
	 * @param id
	 *            - primary key object
	 * @return found entity
	 */
	T find(Object id);
}
