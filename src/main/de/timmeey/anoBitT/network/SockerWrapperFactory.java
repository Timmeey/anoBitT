package de.timmeey.anoBitT.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import de.timmeey.anoBitT.network.impl.ServerSocketWrapper;

public interface SockerWrapperFactory {
	/**
	 * Creates a ServerSocket listening on the specified port. But this is kind
	 * of a workaround if you can't control the local serverSocket. THis
	 * ServerSocket just forwards all data to the local Socket at the
	 * localHostPort
	 * 
	 * Make sure the internal port is already listening
	 * 
	 * @param externalPort
	 *            the external port the socket should listen on
	 * @param internalLocalhostPort
	 *            the internal port where a normal ServerSocket is listening
	 * @return A construct which is already forwarding all traffic from external
	 *         to internal port
	 * @throws IOException
	 */
	public ServerSocketWrapper wrapServerSocket(int internalPort,
			int externalPort) throws IOException;

}
