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
public class SocketFactoryImpl implements SocketFactory, SockerWrapperFactory {
	private final Map<String, Socket> openConnections = new ConcurrentHashMap<String, Socket>();

	@Inject
	public SocketFactoryImpl() {

	}

	@Override
	public ServerSocketWrapper wrapServerSocket(int externalPort,
			int internalLocalhostPort) throws IOException {

		return new ServerSocketWrapper(getServerSocket(externalPort),
				internalLocalhostPort);

	}

	public ServerSocketWrapper createWrappedNonTorServerSocketToLocalServerSocketForward(
			int externalPort, int internalPort) throws IOException {
		return new ServerSocketWrapper(new ServerSocket(externalPort),
				internalPort);
	}

	public ServerSocket getServerSocket(int externalPort) throws IOException {
		return new ServerSocket(externalPort);
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
	public Socket getSocket(String remoteHostname, int remotePort)
			throws UnknownHostException, IOException {
		Socket socket;

		return new Socket(remoteHostname, remotePort);

	}

}
