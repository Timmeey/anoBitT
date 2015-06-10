package de.timmeey.anoBitT.peerGroup;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.tor.KeyPair;

public class PeerGroupApplicationOffer {
	private static final Logger logger = LoggerFactory
			.getLogger(PeerGroupApplicationOffer.class);
	private static KeyPair keyPair;
	private static DHTService dhtService;

	private final String secretOneTimePassword;
	private final PeerGroup peerGroup;
	private final long created;
	private long duration;

	protected PeerGroupApplicationOffer(long duration, PeerGroup peerGroup) {
		this.secretOneTimePassword = Preconditions
				.checkNotNull(RandomStringUtils.randomAlphanumeric(20));
		this.peerGroup = peerGroup;
		this.created = System.currentTimeMillis();
		this.duration = duration;
		logger.info("Setting up the offer, and pushing it into the DHT");
		boolean result = dhtService.put(
				getSecretOneTimePasswordHash(secretOneTimePassword),
				getSecuredOwnOnionAddress(secretOneTimePassword,
						keyPair.getOnionAddress()), true);
		if (result) {
			logger.info("It worked, posted offert to DHT");
		} else {
			logger.warn("Could net Set up the offer. pushing to DHT failed");
			this.duration = -1;// this invalidates the offer
		}

	}

	protected void removeOffer() {
		// remove the offer from dht, but this is optional
		logger.debug("removeOffer() not yet implemented");
		return;
	}

	public String getSecretOneTimePassword() {
		return this.secretOneTimePassword;

	}

	public boolean isStillValiud() {
		return System.currentTimeMillis() < getDeadline();
	}

	public static String getSecretOneTimePasswordHash(
			String secretOneTimePassword) {
		return de.timmeey.libTimmeey.hash.Hashing.sha512(secretOneTimePassword);

	}

	public static String getSecuredOwnOnionAddress(
			String secretOneTimePassword, String onionAddress) {
		String onionSecuredWithOTP = de.timmeey.libTimmeey.hash.Hashing
				.sha512(secretOneTimePassword + onionAddress);
		return String.format("%s:%s", onionSecuredWithOTP, onionAddress);
	}

	public PeerGroup getGroup() {
		return this.peerGroup;

	}

	public long getCreated() {
		return this.created;

	}

	public long getDeadline() {
		return this.created + this.duration;

	}

	public static void setKeyPair(KeyPair keyPair) {
		Preconditions.checkNotNull(keyPair);
		if (PeerGroupApplicationOffer.keyPair == null) {
			PeerGroupApplicationOffer.keyPair = keyPair;
		} else {
			PeerGroupApplicationOffer.logger.error("keyPair was already set");
		}
	}

	public static void setDhtService(DHTService dhtService) {
		Preconditions.checkNotNull(dhtService);
		if (PeerGroupApplicationOffer.dhtService == null) {
			PeerGroupApplicationOffer.dhtService = dhtService;
		} else {
			PeerGroupApplicationOffer.logger
					.error("dhtService was already set");
		}
	}

	private static boolean checkSecuredOnionAddress(String securedOnionAddress,
			String secretOneTimePassword) {
		logger.trace(String
				.format("Checking whether securedOnionAddress %s is valid according to secretOneTimePassword %s",
						securedOnionAddress, secretOneTimePassword));
		try {
			String onionAddress = securedOnionAddress.split(":")[1];
			String hashedPart = securedOnionAddress.split(":")[0];

			System.out.println(onionAddress);
			System.out.println(hashedPart);
			String shouldBeSecuredAddress = getSecuredOwnOnionAddress(
					secretOneTimePassword, onionAddress);
			System.out.println(securedOnionAddress);
			System.out.println(shouldBeSecuredAddress);
			if (shouldBeSecuredAddress.equals(securedOnionAddress)) {
				logger.debug("securedOnionAddress is valid with secretOneTimePassword");
				return true;
			} else {
				logger.debug("securedOnionAddress is NOT valid with secretOneTimePassword! Someone trying to scam us?");
				return false;
			}
		} catch (Exception e) {
			logger.warn(
					"Something went wrong while validation securedOnionAddress {}",
					securedOnionAddress);
			return false;
		}
	}

	public static Optional<String> getValidatedOnionAddressForOffer(
			String secretOneTimePassword, String securedOnionAddress) {
		if (checkSecuredOnionAddress(securedOnionAddress, secretOneTimePassword)) {
			return Optional.of(securedOnionAddress.split(":")[1]);
		} else {
			return Optional.empty();
		}

	}
}
