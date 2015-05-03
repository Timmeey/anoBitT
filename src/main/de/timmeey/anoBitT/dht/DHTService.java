/**
 * 
 */
package de.timmeey.anoBitT.dht;

import java.util.List;

/**
 * @author timmeey
 *
 */
public interface DHTService {

	/**
	 * Puts a key value Pair into the DHT THis method can be used synchronously
	 * by setting waitForConfirmation to true
	 * 
	 * @param key
	 *            The key
	 * @param value
	 *            The value to be stored in the DHT
	 * @param waitForConfirmation
	 *            to set synchronous or asynchronous use
	 * @return true if asynchronous or the result of the operation if
	 *         synchronous
	 */
	public boolean put(String key, String value, boolean waitForConfirmation);

	/**
	 * Retrieves a value from the DHT by Key
	 * 
	 * @param key
	 *            The key of the wanted value
	 * @return the requested value or NULL if not found
	 */
	public String get(String key);

	public List<String> getNodes();

}
