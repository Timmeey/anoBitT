package de.timmeey.anoBitT.peerGroup.Member;

import java.security.PublicKey;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import de.timmeey.anoBitT.Authenticity.TiPublicKey;
import de.timmeey.anoBitT.exceptions.NotOnlineException;
import de.timmeey.anoBitT.peerGroup.PeerGroup;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.exceptions.unchecked.NotYetImplementedException;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse.ResponseCode;

public class PeerGroupMember {
	private final static Logger logger = LoggerFactory
			.getLogger(PeerGroupMember.class);

	private String ipAddress;
	private boolean isOnline;
	private final TiPublicKey publicKey;
	private final String onionAddress;
	private final UUID peerGroup; // Cyclic data structures are not possible
									// due to serialization
	transient private static HTTPRequestService requestService;
	private final int port;
	transient private static KeyPair localKeyPair;
	private static final int REQUEST_WAIT_TIMEOUT = 30;// Seconds

	public PeerGroupMember(PublicKey publicKey, String onionAddress,
			PeerGroup peerGroup, int port, String ip) {
		this.publicKey = new TiPublicKey(publicKey);
		this.onionAddress = onionAddress;
		this.peerGroup = peerGroup.getUUID();
		this.port = port;
		this.ipAddress = ip;

	}

	public PublicKey getPublicKey() {
		return this.publicKey.getPublicKey();
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

	/**
	 * Updates this members IPaddress
	 */
	public void updateIpAddress() {
		logger.debug("Requesting MemberIP");
		PeerGroupMemberIpUpdateRequest req = new PeerGroupMemberIpUpdateRequest(
				this, localKeyPair);
		PeerGroupMemberIpUpdateResponse resp;
		try {
			resp = requestService.send(req, req.getResponseType(), port).get(
					REQUEST_WAIT_TIMEOUT, TimeUnit.SECONDS);
			if (resp.getResponseCode() == ResponseCode.SUCCESS) {
				this.ipAddress = resp.getIpAddress();
				this.isOnline = true;
			} else {
				this.ipAddress = null;
				this.isOnline = false;
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logger.debug("Could not updateIPaddress of member {}");
			isOnline = false;
			this.ipAddress = null;
		}

	}

	public boolean isOnline() {
		try {
			updateIpAddress();
			return isOnline;
		} catch (Exception e) {
			return false;
		}

	}

	public UUID getPeerGroup() {
		return this.peerGroup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String
				.format("PeerGroupMember [ipAddress=%s, isOnline=%s, publicKey=%s, onionAddress=%s, peerGroup=%s, port=%s]",
						ipAddress, isOnline, publicKey.getPublicKey()
								.getAlgorithm(), onionAddress, peerGroup, port);
	}

	public static void setRequestService(HTTPRequestService requestService) {
		Preconditions.checkNotNull(requestService);
		if (PeerGroupMember.requestService == null) {
			PeerGroupMember.requestService = requestService;
		} else {
			PeerGroupMember.logger.error("requestService was already set");
		}
	}

	public static void setKeyPair(KeyPair keyPair) {
		Preconditions.checkNotNull(keyPair);
		if (PeerGroupMember.localKeyPair == null) {
			PeerGroupMember.localKeyPair = keyPair;
		} else {
			PeerGroupMember.logger.error("keyPair was already set");
		}
	}

}
