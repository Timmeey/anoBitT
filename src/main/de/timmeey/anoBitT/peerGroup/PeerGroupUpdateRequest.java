package de.timmeey.anoBitT.peerGroup;

import java.util.UUID;

import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMember;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;

public class PeerGroupUpdateRequest extends
		HTTPRequest<PeerGroupUpdateResponse> {
	transient private final static String path = "/peerGroup/updateRequest";
	private final UUID peerGUuid;

	public UUID getPeerGUuid() {
		return peerGUuid;
	}

	public PeerGroupUpdateRequest(PeerGroupMember recipient, KeyPair auth,
			UUID peerGroupID) {
		super(recipient.getOnionAddress(), path, PeerGroupUpdateResponse.class);
		super.setAuthenticationMap(auth.getAuthMapForMessage(recipient));
		this.peerGUuid = peerGroupID;
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
