package de.timmeey.anoBitT.communication.external;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import timmeeyLib.properties.PropertiesAccessor;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.timmeey.anoBitT.communication.HTTPRequestService;
import de.timmeey.anoBitT.config.GuiceAnnotations.AppProperties;
import de.timmeey.anoBitT.network.SocketFactory;

@Singleton
public class ExternalCommunicationHandler {

	private final HTTPRequestService serializer;
	private final HttpServer server;
	private final PropertiesAccessor props;
	private final SocketFactory socketFactory;

	@Inject
	protected ExternalCommunicationHandler(HttpServer server,
			HTTPRequestService serializer,
			@AppProperties PropertiesAccessor props, SocketFactory socketFactory) {
		this.serializer = serializer;
		this.server = server;
		this.props = props;
		this.socketFactory = socketFactory;
	}

	public ExternalCommunicationHandler startServer(int port)
			throws IOException {
		InetSocketAddress localhost = new InetSocketAddress("127.0.0.1", port);
		server.bind(localhost, 0);
		socketFactory.createWrappedTorServerSocketToLocalServerSocketForward(
				8888, port);
		server.start();
		return this;
	}

	public ExternalCommunicationHandler stopServer(int tmOut) {
		server.stop(tmOut);
		return this;
	}

	public ExternalCommunicationHandler addHttpHandler(String path,
			HttpHandler handler) {
		server.createContext(path, handler);
		return this;
	}

	public ExternalCommunicationHandler removeHttpHandler(String path) {
		server.removeContext(path);
		return this;
	}

}
