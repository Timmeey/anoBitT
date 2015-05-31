package de.timmeey.anoBitT.peerGroup;

import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMember;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;

public class PeerGroupUpdateRequest extends
		HTTPRequest<PeerGroupUpdateResponse> {
	transient private final static String path = "/peerGroup/updateRequest";

	public PeerGroupUpdateRequest(PeerGroupMember recipient, KeyPair auth) {
		super(recipient.getOnionAddress(), path, PeerGroupUpdateResponse.class);
		super.setAuthenticationMap(auth.getAuthMapForMessage(recipient));
	}

	/**
	 * THis method is meant to setup the contexHandlers for the httpRequests.
	 * Its just nice to not have to enter the path at multiple locations. so the
	 * httprequest OBJECT also sets the handler and the path accordingly. Again
	 * just a nice feature, nothing that is enforced
	 * 
	 * @param server
	 */
	public static void addHandler(TimmeeyHttpSimpleServer server,
			HttpHandler handler) {
		server.registerHandler(path, handler);
	}
}
