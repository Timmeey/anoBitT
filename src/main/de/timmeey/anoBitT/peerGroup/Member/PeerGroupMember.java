package de.timmeey.anoBitT.peerGroup.Member;

import java.security.PublicKey;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final PublicKey publicKey;
	private final String onionAddress;
	private final PeerGroup peerGroup;
	private final HTTPRequestService requestService;
	private final int port;
	private final KeyPair localKeyPair;
	private static final int REQUEST_WAIT_TIMEOUT = 30;// Seconds

	public PeerGroupMember(PublicKey publicKey, String onionAddress,
			PeerGroup peerGroup, HTTPRequestService requestService, int port,
			KeyPair localKeyPair) {
		this.publicKey = publicKey;
		this.onionAddress = onionAddress;
		this.peerGroup = peerGroup;
		this.requestService = requestService;
		this.port = port;
		this.localKeyPair = localKeyPair;

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

	public void updateIpAddress() {
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

	public PeerGroup getPeerGroup() {
		return this.peerGroup;
	}

}
