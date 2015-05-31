package de.timmeey.anoBitT.network.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.network.SockerWrapperFactory;
import de.timmeey.libTimmeey.networking.SocketFactory;

public class SocketFactoryImpl implements SocketFactory, SockerWrapperFactory {
	private final Map<String, Socket> openConnections = new ConcurrentHashMap<String, Socket>();
	private static Logger logger = LoggerFactory
			.getLogger(SocketFactoryImpl.class);

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

		logger.trace("Opening normal socket to {} at port {}", remoteHostname,
				remotePort);
		Socket socket = new Socket(remoteHostname, remotePort);
		logger.trace("Socked opened to {} with port {}",
				socket.getInetAddress(), socket.getPort());

		return socket;

	}
}
