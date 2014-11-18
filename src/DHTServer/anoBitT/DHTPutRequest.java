package anoBitT;

import de.timmeey.anoBitT.http.communication.HTTPRequest;

public class DHTPutRequest extends HTTPRequest<DHTReply> {
	final String key;
	final String value;

	public DHTPutRequest(String key, String value) {
		super(DHTReply.class);
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
