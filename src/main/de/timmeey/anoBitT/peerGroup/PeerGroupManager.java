package de.timmeey.anoBitT.peerGroup;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

import de.timmeey.anoBitT.exceptions.InvalidIpAddressException;
import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMember;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;

public class PeerGroupManager {
	private static final Logger logger = LoggerFactory
			.getLogger(PeerGroupManager.class);
	private final KeyPair keyPair;
	private final HTTPRequestService requestService;
	private final int port;
	private String localIp;

	private final List<PeerGroup> peerGroups = Lists.newArrayList();

	public PeerGroupManager(KeyPair keyPair, HTTPRequestService requestService,
			int port, String ip) {
		this.keyPair = keyPair;
		this.requestService = requestService;
		this.port = port;
		this.localIp = ip;
		PeerGroup.setKeyPair(keyPair);
		PeerGroup.setRequestService(requestService);
		PeerGroupMember.setKeyPair(keyPair);
		PeerGroupMember.setRequestService(requestService);

	}

	public Optional<PeerGroup> getPeerGroupByUUID(final UUID uuid) {
		return peerGroups.stream().filter(g -> g.getUUID().equals(uuid))
				.findFirst();
	}

	public PeerGroup createPeerGroup(String name) {
		PeerGroup newGroup = new PeerGroup(name, keyPair, requestService, port,
				localIp);
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
}
