package de.timmeey.anoBitT.peerGroup;

import java.security.PublicKey;

import de.timmeey.anoBitT.Authenticity.TiPublicKey;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;

public class PeerGroupApplicationRequest extends
		HTTPRequest<PeerGroupApplicationResponse> {
	transient private final static String PATH = "/peerGroup/application/request";
	private String secretOneTimePassword;
	private String ownOnionAddress;
	private TiPublicKey pubKey;

	protected PeerGroupApplicationRequest(String host,
			String secretOneTimePassword, String ownOnionAddress,
			PublicKey pubKey) {
		super(host, PATH, PeerGroupApplicationResponse.class);
		this.secretOneTimePassword = secretOneTimePassword;
		this.ownOnionAddress = ownOnionAddress;
		this.pubKey = new TiPublicKey(pubKey);
	}

	public String getSecretOneTimePassword() {
		return secretOneTimePassword;
	}

	public PublicKey getPublicKey() {
		return pubKey.getPublicKey();
	}

	/**
	 * @return the ownOnionAddress
	 */
	public String getOwnOnionAddress() {
		return ownOnionAddress;
	}

	public static void addHandler(TimmeeyHttpSimpleServer server,
			HttpHandler handler) {
		server.registerHandler(PATH, handler);
	}

}
