package de.timmeey.anoBitT.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public interface SocketFactory {

	/**
	 * Creates a ServerSocket
	 * 
	 * @param externalPort
	 *            the external port
	 * @return aServerSocket
	 * @throws IOException
	 */
	public ServerSocket getServerSocket(int port) throws IOException;

	/**
	 * Creates a Socket
	 * 
	 * @param host
	 *            Hostname
	 * @param port
	 *            port
	 * @return anonymous Socket
	 * @throws IOException
	 */
	public Socket getSocket(String hostname, int port) throws IOException;

}
