package de.timmeey.anoBitT.client;

import asg.cliche.Command;
import asg.cliche.Param;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;

public class ConsoleClient {
	private final PeerGroupManager peerGroupManager;
	private final DHTService dhtService;

	public ConsoleClient(PeerGroupManager peerGroupManager,
			DHTService dhtService) {
		super();
		this.peerGroupManager = peerGroupManager;
		this.dhtService = dhtService;
	}

	@Command(description = "Puts a value into the DHT", abbrev = "put")
	public boolean putValueToDHT(
			@Param(name = "key", description = "Key for the value") String key,
			@Param(name = "value", description = "The value") String value,
			@Param(name = "wait", description = "Boolean whether to wait for confirmation or not") boolean wait) {
		boolean result = dhtService.put(key, value, wait);
		System.out.println(result);
		return result;
	}

	@Command(description = "Gets a value from the DHT", abbrev = "get")
	public String getValueFromDHT(
			@Param(name = "key", description = "Key for the value") String key) {
		String value = dhtService.get(key);
		return value;
	}
}
