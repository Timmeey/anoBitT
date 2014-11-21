package de.timmeey.anoBitT.communication;

import java.util.Map;

public class HTTPRequest<T extends HTTPResponse> {
	transient final Class<T> type;
	transient final String host;
	transient final String path;
	private Map<String, String> authenticationMap;

	protected HTTPRequest(String host, String path, Class<T> type) {
		this.host = host;
		this.path = path;
		this.type = type;
	}

	protected HTTPRequest(String host, String path, Class<T> type,
			Map<String, String> authenticationMap) {
		this.host = host;
		this.path = path;
		this.type = type;
	}

	public Class<T> getResponseType() {
		return type;
	}

	public String getHost() {
		return host;
	}

	public String getPath() {
		return path;
	}

	public Map<String, String> getAuthenticationMap() {
		return authenticationMap;
	}

	public HTTPRequest<T> setAuthenticationMap(
			Map<String, String> authenticationMap) {
		this.authenticationMap = authenticationMap;
		return this;
	}
}
