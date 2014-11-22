package anoBitT;

import de.timmeey.libTimmeey.networking.HTTPResponse;

public class DHTReply extends HTTPResponse {
	private final String key;
	private final String value;

	protected DHTReply(String key, String value) {
		this.key = key;
		this.value = value;

	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
