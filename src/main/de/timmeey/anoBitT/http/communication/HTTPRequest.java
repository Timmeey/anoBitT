package de.timmeey.anoBitT.http.communication;

import java.lang.reflect.Type;

public abstract class HTTPRequest<T extends HTTPResponse> {
	final Class<T> type;

	protected HTTPRequest(Class<T> type) {
		this.type = type;
	}

	public Class<T> getResponseType() {
		return type;
	}
}
