package de.timmeey.anoBitT.network.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.impl.NetServerSocket2ServerSocket;
import org.silvertunnel_ng.netlib.api.impl.NetSocket2Socket;
import org.silvertunnel_ng.netlib.api.impl.Socket2NetSocket;
import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePortPrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorNetServerSocket;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.main;
import de.timmeey.anoBitT.network.SockerWrapperFactory;
import de.timmeey.anoBitT.network.SocketFactory;
import de.timmeey.anoBitT.tor.TorManager;
import timmeeyLib.exceptions.unchecked.NotYetInitializedException;

@Singleton
public class AnonSocketFactoryImpl implements SocketFactory,
		SockerWrapperFactory {
	private TorHiddenServicePrivateNetAddress hiddenAddress;
	private NetLayer netLayer;
	private final Map<String, Socket> openConnections = new ConcurrentHashMap<String, Socket>();

	@Inject
	public AnonSocketFactoryImpl() {
		this.hiddenAddress = null;

	}

	// For Security reasons this Factory needs a setter so TorManager does not
	// have to expose the private key
	public AnonSocketFactoryImpl setHiddenAddress(
			TorHiddenServicePrivateNetAddress hiddenAddress) {
		this.hiddenAddress = hiddenAddress;
		return this;
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

		Socket socket = getSocketIfAlreadyExisting(host, port);

		if (socket == null) {
			TcpipNetAddress remoteAddress = new TcpipNetAddress(host, port);
			NetSocket netSocket = this.netLayer.createNetSocket(null, null,
					remoteAddress);
			socket = new NetSocket2Socket(netSocket);
			socket.setKeepAlive(true);
			socket.setSoTimeout(1000 * 60 * 10);

		}
		cacheSocket(socket, host, port);
		return socket;

	}

	private Socket getSocketIfAlreadyExisting(String host, int port) {
		Socket socket = openConnections.get(host + ":" + port);
		if (socket != null && !socket.isClosed()) {
			System.out.println("Found cached socket, reusing it");
			return socket;
		}
		return null;
	}

	private void cacheSocket(Socket socket, String host, int port) {
		openConnections.put(host + ":" + port, socket);
	}

	public void setNetLayer(NetLayer torNetLayer) {
		this.netLayer = netLayer;

	}
}
