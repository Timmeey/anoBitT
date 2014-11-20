package de.timmeey.anoBitT.network;

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
import de.timmeey.anoBitT.tor.TorManager;
import timmeeyLib.exceptions.unchecked.NotYetInitializedException;

@Singleton
public class SocketFactory {
	private TorHiddenServicePrivateNetAddress hiddenAddress;
	private NetLayer netLayer;
	private final Map<String, Socket> openConnections = new ConcurrentHashMap<String, Socket>();

	@Inject
	public SocketFactory() {
		this.hiddenAddress = null;

	}

	// For Security reasons this Factory needs a setter so TorManager does not
	// have to expose the private key
	public SocketFactory setHiddenAddress(
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
	public TorServerSocketForwarder createWrappedTorServerSocketToLocalServerSocketForward(
			int externalPort, int internalLocalhostPort) throws IOException {
		if (hiddenAddress == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");
		return new TorServerSocketForwarder(
				createTorServerSocket(externalPort), internalLocalhostPort);

	}

	public TorServerSocketForwarder createWrappedNonTorServerSocketToLocalServerSocketForward(
			int externalPort, int internalPort) throws IOException {
		return new TorServerSocketForwarder(new ServerSocket(externalPort),
				internalPort);
	}

	/**
	 * Creates a anonymous ServerSocket listening on the hiddenService address
	 * (.onion)
	 * 
	 * @param externalPort
	 *            the external port
	 * @return anonymous ServerSocket listening on the .onion address
	 * @throws IOException
	 */
	public ServerSocket createTorServerSocket(int externalPort)
			throws IOException {
		if (hiddenAddress == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");
		TorHiddenServicePortPrivateNetAddress netAddressWithPort = new TorHiddenServicePortPrivateNetAddress(
				hiddenAddress, externalPort);
		return new NetServerSocket2ServerSocket(
				this.netLayer.createNetServerSocket(null, netAddressWithPort));
	}

	/**
	 * Creates a not anonymous ServerSocket
	 * 
	 * @param externalPort
	 *            the external port
	 * @return aServerSocket
	 * @throws IOException
	 */
	public ServerSocket createNonTorServerSocket(int externalPort)
			throws IOException {
		return new ServerSocket(externalPort);
	}

	/**
	 * Creates a anonymous Socket which is routed over the TOR network. No
	 * special forwarding
	 * 
	 * @param remoteHostname
	 *            Hostname
	 * @param remotePort
	 *            port
	 * @return anonymous Socket
	 * @throws IOException
	 */
	public Socket getPrivateSocket(String remoteHostname, int remotePort)
			throws IOException {
		if (hiddenAddress == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");

		Socket socket = getSocketIfAlreadyExisting(remoteHostname, remotePort);

		if (socket == null) {
			TcpipNetAddress remoteAddress = new TcpipNetAddress(remoteHostname,
					remotePort);
			NetSocket netSocket = this.netLayer.createNetSocket(null, null,
					remoteAddress);
			socket = new NetSocket2Socket(netSocket);
			socket.setKeepAlive(true);
			socket.setSoTimeout(1000 * 60 * 10);

		}
		cacheSocket(socket, remoteHostname, remotePort);
		return socket;

	}

	/**
	 * Creates a normal not anonymous Socket. No special forwarding
	 * 
	 * @param remoteHostname
	 *            Hostname
	 * @param remotePort
	 *            port
	 * @return non anonymous Socket
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Socket getNonPrivateSocket(String remoteHostname, int remotePort)
			throws UnknownHostException, IOException {
		Socket socket = getSocketIfAlreadyExisting(remoteHostname, remotePort);
		if (socket == null) {
			socket = new Socket(remoteHostname, remotePort);

		}
		cacheSocket(socket, remoteHostname, remotePort);
		return new Socket(remoteHostname, remotePort);

	}

	public void setNetLayer(NetLayer torNetLayer) {
		this.netLayer = torNetLayer;

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
}
