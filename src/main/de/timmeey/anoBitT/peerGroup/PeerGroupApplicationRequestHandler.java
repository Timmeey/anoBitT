package de.timmeey.anoBitT.peerGroup;

import java.security.PublicKey;
import java.util.Optional;

import org.bouncycastle.asn1.ocsp.ResponderID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse.ResponseCode;
import de.timmeey.libTimmeey.networking.communicationServer.HttpContext;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;

public class PeerGroupApplicationRequestHandler implements HttpHandler {
	private final Logger logger = LoggerFactory
			.getLogger(PeerGroupApplicationRequestHandler.class);
	private final PeerGroupManager peerGroupManager;

	public PeerGroupApplicationRequestHandler(PeerGroupManager peerGroupManager) {
		this.peerGroupManager = peerGroupManager;

	}

	@Override
	public HttpContext handle(HttpContext context) {
		logger.trace("Handling request for peerGroupOffer");
		PeerGroupApplicationRequest req = context
				.getPayload(PeerGroupApplicationRequest.class);
		String oneTimePassword = req.getSecretOneTimePassword();
		String onionAddress = req.getOwnOnionAddress();
		PublicKey pubKey = req.getPublicKey();

		Optional<PeerGroup> offeredGroup = peerGroupManager
				.findPeerGroupForApplicationOffer(oneTimePassword);
		if (offeredGroup.isPresent()) {
			logger.info(String.format(
					"Got a valid oneTimePassword for group %s from %s",
					offeredGroup.get(), onionAddress));
			offeredGroup.get().addMember(onionAddress, pubKey);
			PeerGroupApplicationResponse resp = new PeerGroupApplicationResponse(
					offeredGroup.get());
			context.setResponse(resp).setResponseCode(ResponseCode.SUCCESS);
			return context;
		} else {
			logger.info(String.format("Got a invalid oneTImePassword, %s",
					oneTimePassword));
			context.setResponseCode(ResponseCode.AUTH_FAILURE);
			return context;
		}
	}
}
