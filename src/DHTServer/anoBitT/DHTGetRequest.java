package anoBitT;

import de.timmeey.anoBitT.communication.HTTPRequest;

public class DHTGetRequest extends HTTPRequest<DHTReply> {
	public final static String path = "dht-service/get";

	private final String key;

	public DHTGetRequest(String key, String host) {
		super(host, path, DHTReply.class);
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
