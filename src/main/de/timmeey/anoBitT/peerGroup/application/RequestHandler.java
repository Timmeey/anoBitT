package de.timmeey.anoBitT.peerGroup.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.libTimmeey.networking.communicationServer.HttpContext;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;

public class RequestHandler implements HttpHandler {
	private final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
	private final PeerGroupApplicationManager applicationManager;

	public RequestHandler(PeerGroupApplicationManager applicationManager) {
		this.applicationManager = applicationManager;

	}

	@Override
	public HttpContext handle(HttpContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
