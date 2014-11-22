package de.timmeey.anoBitT.communication.external;

import java.io.IOException;

import timmeeyLib.networking.SocketFactory;
import timmeeyLib.networking.communicationClient.HTTPRequestService;
import timmeeyLib.networking.communicationServer.HttpHandler;
import timmeeyLib.networking.communicationServer.TimmeeyHttpSimpleServer;
import timmeeyLib.properties.PropertiesAccessor;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.config.GuiceAnnotations.AnonSocketFactory;
import de.timmeey.anoBitT.config.GuiceAnnotations.AppProperties;
import de.timmeey.anoBitT.config.GuiceAnnotations.ExternalHTTPRequestService;

@Singleton
public class ExternalCommunicationHandler {

	private final HTTPRequestService serializer;
	private final TimmeeyHttpSimpleServer server;
	private final PropertiesAccessor props;
	private final SocketFactory socketFactory;

	@Inject
	protected ExternalCommunicationHandler(TimmeeyHttpSimpleServer server,
			@ExternalHTTPRequestService HTTPRequestService serializer,
			@AppProperties PropertiesAccessor props,
			@AnonSocketFactory SocketFactory socketFactory) {
		this.serializer = serializer;
		this.server = server;
		this.props = props;
		this.socketFactory = socketFactory;
	}

	public ExternalCommunicationHandler startServer(int port)
			throws IOException {
		server.setServerSocket(socketFactory.getServerSocket(port));

		return this;
	}

	public ExternalCommunicationHandler stopServer(int tmOut) {
		server.stop();
		return this;
	}

	public ExternalCommunicationHandler addHttpHandler(String path,
			HttpHandler handler) {
		server.registerHandler(path, handler);
		return this;
	}

	public ExternalCommunicationHandler removeHttpHandler(String path) {
		server.unregister(path);
		return this;
	}

}
