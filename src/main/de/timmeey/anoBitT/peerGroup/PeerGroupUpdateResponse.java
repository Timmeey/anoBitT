package de.timmeey.anoBitT.peerGroup;

import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse;

public class PeerGroupUpdateResponse extends HTTPResponse {

	private final PeerGroup peerGroup;

	protected PeerGroupUpdateResponse(PeerGroup peerGroup) {
		this.peerGroup = peerGroup;
	}

	public PeerGroup getPeerGroup() {
		return this.peerGroup;
	}

}
