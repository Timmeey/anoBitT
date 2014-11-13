package de.timmeey.anoBitT.network.portSocketForwarder;

import java.io.IOException;
import java.net.UnknownHostException;

import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.api.NetSocket;
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

	// public SocketFactory(TorHiddenServicePrivateNetAddress hiddenAddressConf)
	// {
	// hiddenAddress = hiddenAddressConf;
	// }

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
	public TorServerSocketForwarder createTorServerSocketToLocalServerSocketForward(
			int externalPort, int internalLocalhostPort) throws IOException {
		if (hiddenAddress == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");
		return new TorServerSocketForwarder(
				createTorServerSocket(externalPort), internalLocalhostPort);

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
	public TorNetServerSocket createTorServerSocket(int externalPort)
			throws IOException {
		if (hiddenAddress == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");
		TorHiddenServicePortPrivateNetAddress netAddressWithPort = new TorHiddenServicePortPrivateNetAddress(
				hiddenAddress, externalPort);
		return (TorNetServerSocket) main.torNetLayer.createNetServerSocket(
				null, netAddressWithPort);
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
	public PrivateSocket createPrivateSocket(String remoteHostname,
			int remotePort) throws IOException {
		if (hiddenAddress == null)
			throw new NotYetInitializedException(
					"Socketfactory is not yet initialized");
		TcpipNetAddress remoteAddress = new TcpipNetAddress(remoteHostname,
				remotePort);
		NetSocket netSocket = main.torNetLayer.createNetSocket(null, null,
				remoteAddress);
		return new PrivateSocket(netSocket);
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
	public NonPrivateSocket createNonPrivateSocket(String remoteHostname,
			int remotePort) throws UnknownHostException, IOException {
		return new NonPrivateSocket(remoteHostname, remotePort);

	}
}
