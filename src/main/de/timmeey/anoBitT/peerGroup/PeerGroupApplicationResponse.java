package de.timmeey.anoBitT.peerGroup;

import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse;

public class PeerGroupApplicationResponse extends HTTPResponse {

	private final PeerGroup peerGroup;

	public PeerGroupApplicationResponse(PeerGroup peerGroup) {
		super();
		this.peerGroup = peerGroup;
	}

	/**
	 * @return the peerGroup
	 */
	public PeerGroup getPeerGroup() {
		return peerGroup;
	}

}
