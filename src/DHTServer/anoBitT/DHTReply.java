package anoBitT;

import com.google.gson.Gson;
import com.google.inject.Inject;

import de.timmeey.anoBitT.http.communication.HTTPResponse;

public class DHTReply extends HTTPResponse {
	private String key;
	private String value;
	@Inject
	public DHTReply(Gson gson) {
		super(gson);

	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}



}
