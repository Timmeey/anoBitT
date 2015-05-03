package de.timmeey.anoBitT.peerGroup.application;

import de.timmeey.libTimmeey.networking.communicationServer.HTTPRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;

public class Request extends HTTPRequest<Response> {
	transient private final static String PATH = "/peerGroup/application/request";
	private String secretOneTimePassword;
	private String ownPublicKey;

	protected Request(String host, String secretOneTimePassword,
			String ownPublicKey) {
		super(host, PATH, Response.class);
		this.secretOneTimePassword = secretOneTimePassword;
		this.ownPublicKey = ownPublicKey;
	}

	public String getSecretOneTimePassword() {
		return secretOneTimePassword;
	}

	public String getOwnPublicKey() {
		return ownPublicKey;
	}

	public static void addHandler(TimmeeyHttpSimpleServer server,
			HttpHandler handler) {
		server.registerHandler(PATH, handler);
	}

}
