package ua.skillsup.practice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Implement service and write unit tests for it
 * For storing data service has to call {@link ExampleDao#store(ExampleEntity)} method
 * For retrieving stored data service has to call {@link ExampleDao#findAll()} method
 */
public interface ExampleService {

	/**
	 * Process and store new item to the system
	 * @param title the title of an item, has to be unique, available length range 3-20, mandatory field
	 * @param price the price of an item, in case of scale mre than 3 should be rounded 2 decimal points
	 *              with half up rule, lowest limit is at 15.00, mandatory field
	 */
	void addNewItem(String title, BigDecimal price);

	/**
	 * Prepare storage statistic of items average prices per day they were added.
	 * In case no items were added in concrete day - the day shouldn't be present in the final result
	 * @return {@link Map} of statistic results, where key is the day when items were stored, and
	 *      the value is actual average cost of all items stored during that day
	 */
	Map<LocalDate, BigDecimal> getStatistic();
}