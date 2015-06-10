package de.timmeey.anoBitT.peerGroup.Member;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.peerGroup.PeerGroup;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.anoBitT.peerGroup.PeerGroupUpdateRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse.ResponseCode;
import de.timmeey.libTimmeey.networking.communicationServer.HttpContext;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;

public class PeerGroupMemberIpUpdateRequestHandler implements HttpHandler {
	private final PeerGroupManager peerGroupManager;
	private final static Logger logger = LoggerFactory
			.getLogger(PeerGroupMemberIpUpdateRequestHandler.class);

	public PeerGroupMemberIpUpdateRequestHandler(
			PeerGroupManager peerGroupManager) {
		super();
		this.peerGroupManager = peerGroupManager;
	}

	@Override
	public HttpContext handle(HttpContext context) {

		PeerGroupUpdateRequest request = context
				.getPayload(PeerGroupUpdateRequest.class);
		UUID groupUUID = request.peerGroupUuid();
		Optional<PeerGroup> requestedGroup = peerGroupManager
				.getPeerGroupByUUID(groupUUID);
		logger.trace("Requested UUID was {}", groupUUID);

		if (requestedGroup.isPresent()) {
			logger.trace("Requested Group was found");
			boolean isAuthorized = requestedGroup.get()
					.isAuthMapFromAuthorizedMember(
							request.getAuthenticationMap());
			if (isAuthorized) {
				logger.trace("Request was authorized");
				context.setResponse(new PeerGroupMemberIpUpdateResponse(
						peerGroupManager.getLocalIp()));
				context.setResponseCode(ResponseCode.SUCCESS);
			} else {
				logger.info("Request was not authorized");
				context.setResponseCode(ResponseCode.AUTH_FAILURE);
			}
		} else {
			context.setResponseCode(ResponseCode.FAILURE);

		}
		logger.trace("Handler finished");
		return context;
	}
}
