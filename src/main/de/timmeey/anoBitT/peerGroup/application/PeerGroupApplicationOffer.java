package de.timmeey.anoBitT.peerGroup.application;

import org.apache.commons.lang3.RandomStringUtils;

import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.peerGroup.PeerGroup;
import de.timmeey.anoBitT.tor.KeyPair;

public class PeerGroupApplicationOffer {
	private final String secretOneTimePassword;
	private final PeerGroup betreffendePeerGroup;
	private final long created;
	private final long duration;
	private final String ownOnionAddr;
	private final DHTService dhtService;

	protected PeerGroupApplicationOffer(DHTService dhtService, long duration,
			PeerGroup peerGroup, String ownOnionAddr) {
		this.secretOneTimePassword = RandomStringUtils.randomAlphanumeric(20);
		this.betreffendePeerGroup = peerGroup;
		this.created = System.currentTimeMillis();
		this.duration = duration;
		this.dhtService = dhtService;
		this.ownOnionAddr = ownOnionAddr;
		this.dhtService.put(getSecretOneTimePasswordHash(),
				getSecuredOwnOnionAddress(), false);

	}

	protected void removeOffer() {
		// remove the offer from dht, but this is optional
		return;
	}

	public String getSecretOneTimePassword() {
		return this.secretOneTimePassword;

	}

	public String getSecretOneTimePasswordHash() {
		return de.timmeey.libTimmeey.hash.Hashing
				.sha512(getSecretOneTimePassword());

	}

	public String getSecuredOwnOnionAddress() {
		String onionSecuredWithOTP = de.timmeey.libTimmeey.hash.Hashing
				.sha512(secretOneTimePassword + ownOnionAddr);
		return String.format("%s:%s", onionSecuredWithOTP, ownOnionAddr);
	}

	public PeerGroup getBetreffendePeerGroup() {
		return this.betreffendePeerGroup;

	}

	public long getCreated() {
		return this.created;

	}

	public long getDeadline() {
		return this.created + this.duration;

	}

}
