package de.timmeey.anoBitT.peerGroup.management;

import org.apache.commons.lang3.RandomStringUtils;

import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.peerGroup.PeerGroup;

public class PeerGroupApplicationOffer {
	private final String secretOneTimePassword;
	private final PeerGroup betreffendePeerGroup;
	private final long created;
	private final long duration;
	private final DHTService dhtService;

	protected PeerGroupApplicationOffer(DHTService dhtService, long duration,
			PeerGroup peerGroup, String ownOnionAddress) {
		this.secretOneTimePassword = RandomStringUtils.randomAlphanumeric(20);
		this.betreffendePeerGroup = peerGroup;
		this.created = System.currentTimeMillis();
		this.duration = duration;
		this.dhtService = dhtService;
		this.dhtService.put(getSecretOneTimePasswordHash(), ownOnionAddress,
				false);

	}

	protected void removeOffer() {
		// remove the offer from dht, but this is optional
		return;
	}

	public String getSecretOneTimePassword() {
		return this.secretOneTimePassword;

	}

	public String getSecretOneTimePasswordHash() {
		return timmeeyLib.hash.Hashing.sha512(getSecretOneTimePassword());

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
