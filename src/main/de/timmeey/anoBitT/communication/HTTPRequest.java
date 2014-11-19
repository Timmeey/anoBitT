package de.timmeey.anoBitT.communication;

import java.lang.reflect.Type;

public abstract class HTTPRequest<T extends HTTPResponse> {
	transient final Class<T> type;
	transient final String host;
	transient final String path;

	protected HTTPRequest(String host, String path, Class<T> type) {
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
}
