package de.timmeey.anoBitT.peerGroup.Member;

import java.util.UUID;

import com.google.common.base.Preconditions;

import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;

public class PeerGroupMemberIpUpdateRequest extends
		HTTPRequest<PeerGroupMemberIpUpdateResponse> {
	transient private final static String path = "/peerGroup/updateIP";
	private final UUID peerGroupUuid;

	protected PeerGroupMemberIpUpdateRequest(PeerGroupMember recipient,
			KeyPair auth) {
		super(recipient.getOnionAddress(), path,
				PeerGroupMemberIpUpdateResponse.class);
		Preconditions.checkNotNull(auth);
		Preconditions.checkNotNull(recipient);
		super.setAuthenticationMap(auth.getAuthMapForMessage(recipient));
		this.peerGroupUuid = recipient.getPeerGroup();
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
