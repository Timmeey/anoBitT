package de.timmeey.anoBitT.torrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinimalPeer {

	private static final Logger logger = LoggerFactory
			.getLogger(MinimalPeer.class);

	private byte[] peerID;

	private String address;

	public MinimalPeer(byte[] peerID, String address) {
		this.peerID = peerID;
		this.address = address;

	}

	public byte[] getPeerID() {
		return peerID;
	}

	public MinimalPeer setPeerID(byte[] peerID) {
		this.peerID = peerID;
		return this;
	}

	public String getAddress() {
		return address;
	}

	public MinimalPeer setAddress(String address) {
		this.address = address;
		return this;
	}

}
