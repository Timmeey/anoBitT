package de.timmeey.anoBitT.peerGroup.Member;

import java.util.UUID;

import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPRequest;

public class PeerGroupMemberIpUpdateRequest extends
		HTTPRequest<PeerGroupMemberIpUpdateResponse> {
	transient private final static String path = "/peerGroup/updateIP";
	private UUID peerGroupUuid;

	protected PeerGroupMemberIpUpdateRequest(PeerGroupMember recipient,
			KeyPair auth) {
		super(recipient.getOnionAddress(), path,
				PeerGroupMemberIpUpdateResponse.class);
		super.setAuthenticationMap(auth.getAuthMapForMessage(recipient));
		this.peerGroupUuid = recipient.getPeerGroup().getUUID();
	}

}
