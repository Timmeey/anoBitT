package de.timmeey.anoBitT.communication.external;

import java.io.IOException;

import de.timmeey.libTimmeey.networking.NetSerializer;
import de.timmeey.libTimmeey.networking.SocketFactory;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;

import static com.google.common.base.Preconditions.checkNotNull;

public class ExternalCommunicationHandler {

	private final TimmeeyHttpSimpleServer server;

	public ExternalCommunicationHandler(NetSerializer serializer,
			PropertiesAccessor props, SocketFactory socketFactory)
			throws NumberFormatException, IOException {
		checkNotNull(serializer);
		checkNotNull(socketFactory);
		checkNotNull(props);

		this.server = new TimmeeyHttpSimpleServer(serializer,
				socketFactory.getServerSocket(Integer.parseInt(props
						.getProperty("commPort", "8888"))));
	}

	public ExternalCommunicationHandler startServer(int port)
			throws IOException {
		server.startServer();

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
