package de.timmeey.anoBitT.torrent;

import java.util.Arrays;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((address == null) ? 0 : address.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MinimalPeer other = (MinimalPeer) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

}
