package ua.skillsup.practice;

import java.util.List;

public interface ExampleDao {

	/**
	 * Stiores given entity
	 * @param entity an entity to store
	 * @return true if entity was stored successfully, false if current entity is already in storage
	 * @throws ExampleNetworkException in case any issue occurred with storage connection
	 */
	boolean store(ExampleEntity entity) throws ExampleNetworkException;

	/**
	 * Retrieve all stored entities
	 * @return {@link java.util.List} of {@link ExampleEntity}
	 * @throws ExampleNetworkException in case any issue occurred with storage connection
	 */
	List<ExampleEntity> findAll() throws ExampleNetworkException;
}