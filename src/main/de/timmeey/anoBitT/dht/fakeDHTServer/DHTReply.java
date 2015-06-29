package de.timmeey.anoBitT.dht.fakeDHTServer;

import java.util.List;

import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse;

public class DHTReply extends HTTPResponse {
	private final String key;
	private final List<String> value;

	protected DHTReply(String key, List<String> value) {
		this.key = key;
		this.value = value;

	}

	public String getKey() {
		return key;
	}

	public List<String> getValue() {
		return value;
	}

}
