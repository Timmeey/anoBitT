package anoBitT;

public class DHTPutRequest {
	String key;
	String value;

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public DHTReply(String key, String value) {
		this.key = key;
		this.value = value;
	}

}
