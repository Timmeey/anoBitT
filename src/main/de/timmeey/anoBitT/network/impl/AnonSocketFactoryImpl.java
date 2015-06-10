package de.timmeey.anoBitT.network.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.impl.NetServerSocket2ServerSocket;
import org.silvertunnel_ng.netlib.api.impl.NetSocket2Socket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePortPrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePrivateNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

import de.timmeey.anoBitT.network.SockerWrapperFactory;
import de.timmeey.libTimmeey.exceptions.unchecked.NotYetInitializedException;
import de.timmeey.libTimmeey.networking.SocketFactory;

public class AnonSocketFactoryImpl implements SocketFactory,
		SockerWrapperFactory {
	private TorHiddenServicePrivateNetAddress hiddenAddress;
	private NetLayer netLayer;
	private final Map<String, Socket> openConnections = new ConcurrentHashMap<String, Socket>();
	private static Logger logger = LoggerFactory
			.getLogger(AnonSocketFactoryImpl.class);

	public AnonSocketFactoryImpl(NetLayer torNetLayer,
			TorHiddenServicePrivateNetAddress hiddenAddress) {
		this.hiddenAddress = checkNotNull(hiddenAddress);
		this.netLayer = checkNotNull(torNetLayer);
		logger.debug("Server Sockets will listen on {}",
				hiddenAddress.getPublicOnionHostname());

	}

	/**
	 * Creates a anonymous ServerSocket listening on the specified port. But
	 * this is kind of a workaround if you can't control the local serverSocket.
	 * THis anonymous ServerSocket just forwards all data to the local non
	 * anonymous Socket at the localHostPort
	 * 
	 * Make sure the internal port is already listening
	 * 
	 * @param externalPort
	 *            the external port the socket should listen on
	 * @param internalLocalhostPort
	 *            the internal port where a normal non anonymous ServerSocket is
	 *            listening
	 * @return A construct which is already forwarding all traffic from external
	 *         to internal port
	 * @throws IOException
	 */
	public ServerSocketWrapper wrapServerSocket(int externalPort,
			int internalLocalhostPort) throws IOException {
		if (hiddenAddress == null || netLayer == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");
		return new ServerSocketWrapper(getServerSocket(externalPort),
				internalLocalhostPort);

	}

	public ServerSocket getServerSocket(int externalPort) throws IOException {
		if (hiddenAddress == null || netLayer == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");
		TorHiddenServicePortPrivateNetAddress netAddressWithPort = new TorHiddenServicePortPrivateNetAddress(
				hiddenAddress, externalPort);
		return new NetServerSocket2ServerSocket(
				this.netLayer.createNetServerSocket(null, netAddressWithPort));
	}

	public Socket getSocket(String host, int port) throws IOException {
		if (hiddenAddress == null || netLayer == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");

		logger.trace("Opening TOR socket to {} at port {}", host, port);
		Socket socket;

		TcpipNetAddress remoteAddress = new TcpipNetAddress(host, port);
		NetSocket netSocket = this.netLayer.createNetSocket(null, null,
				remoteAddress);
		logger.debug("Opened TOR socket to {} at port {}", host, port);
		socket = new NetSocket2Socket(netSocket);
		socket.setKeepAlive(true);
		socket.setSoTimeout(1000 * 60 * 10);

		return socket;

	}
}
