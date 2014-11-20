package de.timmeey.anoBitT.communication.httpServer;

public class AnonBitMessage {
	String path;
	String payloadObject;

	public AnonBitMessage(String path, String payload) {
		this.path = path;
		this.payloadObject = payload;
	}

	public String getPath() {
		return path;
	}

	public String getPayloadObject() {
		return payloadObject;
	}

}