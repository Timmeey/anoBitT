package de.timmeey.anoBitT.network.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import timmeeyLib.networking.SocketFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.network.SockerWrapperFactory;

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
