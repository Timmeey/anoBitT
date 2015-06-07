package de.timmeey.anoBitT.peerGroup;

import java.util.UUID;

import de.timmeey.libTimmeey.networking.communicationServer.HttpContext;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse.ResponseCode;

public class PeerGroupUpdateRequestHandler implements HttpHandler {
	private final PeerGroupManager peerGroupManager;

	public PeerGroupUpdateRequestHandler(PeerGroupManager peerGroupManager) {
		super();
		this.peerGroupManager = peerGroupManager;
	}

	@Override
	public HttpContext handle(HttpContext context) {
		PeerGroupUpdateRequest request = context
				.getPayload(PeerGroupUpdateRequest.class);
		UUID groupUUID = request.getPeerGUuid();
		PeerGroup requestedGroup = peerGroupManager
				.getPeerGroupByUUID(groupUUID);
		if (requestedGroup != null) {
			context.setResponse(new PeerGroupUpdateResponse(requestedGroup));
			context.setResponseCode(ResponseCode.SUCCESS);
		}
		return context;
	}
}
