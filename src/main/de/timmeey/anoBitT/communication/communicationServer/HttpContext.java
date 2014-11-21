package de.timmeey.anoBitT.communication.communicationServer;

import com.google.gson.Gson;

import de.timmeey.anoBitT.communication.HTTPRequest;
import de.timmeey.anoBitT.communication.HTTPResponse;

public class HttpContext {
	private final Gson gson;
	private String payload;
	private HTTPResponse response = new HTTPResponse();

	private int responseCode;

	public HttpContext(Gson gson, String payloadObject) {
		this.payload = payloadObject;
		this.gson = gson;
	}

	public void setResponse(HTTPResponse response) {
		this.response = response;
	}

	public <T extends HTTPRequest<?>> T getPayload(Class<T> clazz) {
		return gson.fromJson(payload, clazz);
	}

	public String getResponse() {
		response.setResponseCode(getResponseCode());
		return gson.toJson(response);
	}

	public HttpContext setResponseCode(int responseCode) {
		this.responseCode = responseCode;
		return this;
	}

	public int getResponseCode() {
		return this.responseCode;
	}
}
