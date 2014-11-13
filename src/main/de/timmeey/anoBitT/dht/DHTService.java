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
	
	public DHTService put(String key, String value);
	
	public String get(String key);
	
	public List<String> getNodes();

}
