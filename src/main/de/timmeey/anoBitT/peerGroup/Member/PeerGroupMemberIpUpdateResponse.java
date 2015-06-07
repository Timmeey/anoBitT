package de.timmeey.anoBitT.peerGroup.Member;

import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse;

public class PeerGroupMemberIpUpdateResponse extends HTTPResponse {

	private String ipAddress;

	public PeerGroupMemberIpUpdateResponse(String ipAddress) {
		super();
		this.ipAddress = ipAddress;
	}

	public String getIpAddress() {
		return ipAddress;
	}

}
