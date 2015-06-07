package de.timmeey.anoBitT.peerGroup.Member;

import java.security.PublicKey;
import java.util.concurrent.Future;

import de.timmeey.anoBitT.exceptions.NotOnlineException;
import de.timmeey.libTimmeey.exceptions.unchecked.NotYetImplementedException;

public class PeerGroupMember {

	private String ipAddress;
	private boolean isOnline;
	private final PublicKey publicKey;
	private final String onionAddress;

	public PeerGroupMember(PublicKey publicKey, String onionAddress) {
		this.publicKey = publicKey;
		this.onionAddress = onionAddress;
	}

	public PublicKey getPublicKey() {
		return this.publicKey;
	}

	public String getIpAddress() throws NotOnlineException {
		if (isOnline) {
			return this.ipAddress;
		} else {
			throw new NotOnlineException();
		}
	}

	public String getOnionAddress() {
		return this.onionAddress;
	}

	public Future<PeerGroupMember> updateIpAddress() {
		// TODO needs to be implemented
		throw new NotYetImplementedException();
	}

	public boolean isOnline() {
		return this.isOnline;

	}

}
