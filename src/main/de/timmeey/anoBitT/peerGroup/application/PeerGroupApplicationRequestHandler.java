package de.timmeey.anoBitT.peerGroup.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.libTimmeey.networking.communicationServer.HttpContext;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;

public class PeerGroupApplicationRequestHandler implements HttpHandler {
	private final Logger logger = LoggerFactory.getLogger(PeerGroupApplicationRequestHandler.class);
	private final PeerGroupApplicationManager applicationManager;

	public PeerGroupApplicationRequestHandler(PeerGroupApplicationManager applicationManager) {
		this.applicationManager = applicationManager;

	}

	@Override
	public HttpContext handle(HttpContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
