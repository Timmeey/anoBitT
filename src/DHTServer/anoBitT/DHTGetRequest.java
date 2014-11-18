package anoBitT;

import de.timmeey.anoBitT.http.communication.HTTPRequest;

public class DHTGetRequest extends HTTPRequest<DHTReply> {

	private final String key;

	protected DHTGetRequest(String key) {
		super(DHTReply.class);
		this.key = key;
	}

	public String getKey() {
		return key;
	}

}
