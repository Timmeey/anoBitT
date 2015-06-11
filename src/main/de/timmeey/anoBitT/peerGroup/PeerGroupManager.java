package de.timmeey.anoBitT.peerGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.exceptions.InvalidIpAddressException;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse.ResponseCode;

public class PeerGroupManager {
	private static final Logger logger = LoggerFactory
			.getLogger(PeerGroupManager.class);
	private final KeyPair keyPair;
	private final HTTPRequestService requestService;
	private final DHTService dhtService;
	private final int port;
	private String localIp;

	private final List<PeerGroup> peerGroups = Lists.newArrayList();

	public PeerGroupManager(KeyPair keyPair, HTTPRequestService requestService,
			int port, String ip, DHTService dhtService) {
		this.keyPair = keyPair;
		this.requestService = requestService;
		this.port = port;
		this.localIp = ip;
		this.dhtService = dhtService;
		PeerGroup.setKeyPair(keyPair);
		PeerGroup.setRequestService(requestService);
		PeerGroup.setPort(port);
		PeerGroupApplicationOffer.setKeyPair(keyPair);
		PeerGroupApplicationOffer.setDhtService(dhtService);

	}

	public Optional<PeerGroup> getPeerGroupByUUID(final UUID uuid) {
		return peerGroups.stream().filter(g -> g.getUUID().equals(uuid))
				.findFirst();
	}

	public PeerGroup createPeerGroup(String name) {
		PeerGroup newGroup = new PeerGroup(name);
		logger.info("Creating new PeerGroup {}", name);
		this.peerGroups.add(newGroup);
		return newGroup;
	}

	public String getLocalIp() {
		return this.localIp;
	}

	/**
	 * Needs to get called whenever the localIp changes
	 * 
	 * @param ip
	 * @return
	 * @throws InvalidIpAddressException
	 */
	public PeerGroupManager setLocalIp(String ip)
			throws InvalidIpAddressException {
		if (InetAddresses.isInetAddress(ip)) {
			this.localIp = ip;
			return this;
		} else {
			throw new InvalidIpAddressException(String.format(
					"%s is not a valid IPv4 Address", ip));
		}

	}

	public Optional<PeerGroup> findPeerGroupForApplicationOffer(
			String oneTimePassword) {
		return this.peerGroups
				.stream()
				.filter(pg -> pg
						.hasApplicationOfferForOneTImePassword(oneTimePassword))
				.findFirst();
	}

	public Optional<PeerGroup> tryToJoinWithOffer(String secretOneTimePassword) {
		String securedOnionAddress = dhtService.get(PeerGroupApplicationOffer
				.getSecretOneTimePasswordHash(secretOneTimePassword));
		if (securedOnionAddress != null) {
			logger.debug("Got some value for our secretOneTimePassword: %s",
					securedOnionAddress);
			Optional<String> onionAddress = PeerGroupApplicationOffer
					.getValidatedOnionAddressForOffer(secretOneTimePassword,
							securedOnionAddress);
			if (onionAddress.isPresent()) {
				logger.info(
						"It seems like we have a valid offer here. Sending reqeust to Group %s",
						onionAddress.get());
				PeerGroupApplicationRequest joinRequest = new PeerGroupApplicationRequest(
						onionAddress.get(), secretOneTimePassword,
						keyPair.getOnionAddress(), keyPair.getPublicKey());
				try {
					PeerGroupApplicationResponse response = requestService
							.send(joinRequest, joinRequest.getResponseType(),
									port).get(30, TimeUnit.SECONDS);
					if (response.getResponseCode() == ResponseCode.SUCCESS) {
						logger.info("Woho, PeerGroup confirmed our request to join");
						PeerGroup newPeerGroup = response.getPeerGroup();
						this.peerGroups.add(newPeerGroup);
						return Optional.of(newPeerGroup);
					} else {
						logger.debug("Response code was: {}",
								response.getResponseCode());
					}

				} catch (InterruptedException | ExecutionException
						| TimeoutException e) {
					logger.info("Request to join peer group did not work", e);
				}

			}
		}
		logger.info("Seems like there was no valid offer for the provided secretOneTimePassword :-(");
		return Optional.empty();

	}
}
