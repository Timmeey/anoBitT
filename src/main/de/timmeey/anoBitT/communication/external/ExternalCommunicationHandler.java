package de.timmeey.anoBitT.communication.external;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.config.GuiceAnnotations.AnonSocketFactory;
import de.timmeey.anoBitT.config.GuiceAnnotations.AppProperties;
import de.timmeey.anoBitT.config.GuiceAnnotations.ExternalHTTPRequestService;
import de.timmeey.libTimmeey.networking.SocketFactory;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;

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
