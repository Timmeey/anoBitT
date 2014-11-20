package de.timmeey.anoBitT.communication.httpServer;

import com.google.gson.Gson;

import de.timmeey.anoBitT.communication.HTTPRequest;
import de.timmeey.anoBitT.communication.HTTPResponse;

public class HttpContext {
	private final Gson gson;
	private String payload;
	private String response;

	public HttpContext(Gson gson, String payloadObject) {
		this.payload = payloadObject;
		this.gson = gson;
	}

	public void setResponse(HTTPResponse response) {
		this.response = gson.toJson(response);
	}

	public <T extends HTTPRequest<?>> T getPayload(Class<T> clazz) {
		return gson.fromJson(payload, clazz);
	}

	public String getResponse() {
		return response;
	}

}
