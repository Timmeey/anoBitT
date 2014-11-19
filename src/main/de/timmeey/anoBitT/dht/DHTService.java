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

	public boolean put(String key, String value, boolean waitForConfirmation);

	public String get(String key);

	public List<String> getNodes();

}
