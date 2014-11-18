package anoBitT;

import de.timmeey.anoBitT.http.communication.HTTPResponse;

public class DHTReply implements HTTPResponse {
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
