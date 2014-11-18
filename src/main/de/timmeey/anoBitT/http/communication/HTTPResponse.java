package de.timmeey.anoBitT.http.communication;

import com.google.gson.Gson;

public class HTTPResponse {
	transient protected final Gson gson;

	public HTTPResponse(Gson gson) {
		this.gson = gson;

	}

	public String getTransferRepresentation() {
		return gson.toJson(this);
	}
	
	@Override
	public String toString() {
		return this.getTransferRepresentation();
	}
}
