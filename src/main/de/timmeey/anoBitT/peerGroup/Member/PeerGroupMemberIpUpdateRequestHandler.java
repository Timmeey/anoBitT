package de.timmeey.anoBitT.peerGroup.Member;

import java.util.Optional;
import java.util.UUID;

import de.timmeey.anoBitT.peerGroup.PeerGroup;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.anoBitT.peerGroup.PeerGroupUpdateRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HttpContext;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse.ResponseCode;

public class PeerGroupMemberIpUpdateRequestHandler implements HttpHandler {
	private final PeerGroupManager peerGroupManager;

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

		if (requestedGroup.isPresent()) {
			boolean isAuthorized = requestedGroup.get()
					.isAuthMapFromAuthorizedMember(
							request.getAuthenticationMap());
			if (isAuthorized) {
				context.setResponse(new PeerGroupMemberIpUpdateResponse(
						"127.0.0.1"));
				context.setResponseCode(ResponseCode.SUCCESS);
			} else {
				context.setResponseCode(ResponseCode.AUTH_FAILURE);
			}
		} else {
			context.setResponseCode(ResponseCode.FAILURE);

		}
		return context;
	}
}
