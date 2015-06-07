package de.timmeey.anoBitT.peerGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;

public class PeerGroupManager {
	private static final Logger logger = LoggerFactory
			.getLogger(PeerGroupManager.class);
	private final KeyPair keyPair;
	private final HTTPRequestService requestService;
	private final int port;

	private final List<PeerGroup> peerGroups = Lists.newArrayList();

	public PeerGroupManager(KeyPair keyPair, HTTPRequestService requestService,
			int port) {
		this.keyPair = keyPair;
		this.requestService = requestService;
		this.port = port;
	}

	public Optional<PeerGroup> getPeerGroupByUUID(final UUID uuid) {
		return peerGroups.stream().filter(g -> g.getUUID().equals(uuid))
				.findFirst();
	}

	public PeerGroup createPeerGroup(String name) {
		PeerGroup newGroup = new PeerGroup(name, keyPair, requestService, port);
		logger.info("Creating new PeerGroup {}", name);
		this.peerGroups.add(newGroup);
		return newGroup;
	}
}
