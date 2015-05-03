package de.timmeey.anoBitT.network.impl;

/** 
 * This program is an example from the book "Internet 
 * programming with Java" by Svetlin Nakov. It is freeware. 
 * For more information: http://www.nakov.com/books/inetjava/ 
 * 
 * 
 * TCPForwardServer is a simple TCP bridging software that 
 * allows a TCP port on some host to be transparently forwarded 
 * to some other TCP port on some other host. TCPForwardServer 
 * continuously accepts client connections on the listening TCP 
 * port (source port) and starts a thread (ClientThread) that 
 * connects to the destination host and starts forwarding the 
 * data between the client socket and destination socket. 

 */
import java.io.*;
import java.net.*;

import org.silvertunnel_ng.netlib.api.NetSocket;
import org.silvertunnel_ng.netlib.api.impl.Socket2NetSocket;
import org.silvertunnel_ng.netlib.layer.tor.TorNetServerSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerSocketWrapper extends Thread {
	private static final Logger logger = LoggerFactory
			.getLogger(ServerSocketWrapper.class);
	private ServerSocket torSocket;
	private int localHostPort;

	protected ServerSocketWrapper(ServerSocket torSocket, int localhostPort) {
		this.torSocket = torSocket;
		this.localHostPort = localhostPort;
		this.start();

	}

	public void run() {
		while (true) {
			Socket clientSocket;
			try {
				logger.trace("Waiting to accept");
				clientSocket = torSocket.accept();

				logger.debug("Accepted");
				ClientThread clientThread = new ClientThread(clientSocket,
						localHostPort);
				clientThread.start();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	/**
	 * ClientThread is responsible for starting forwarding between the client
	 * and the server. It keeps track of the client and servers sockets that are
	 * both closed on input/output error durinf the forwarding. The forwarding
	 * is bidirectional and is performed by two ForwardThread instances.
	 */
	class ClientThread extends Thread {
		private Socket mClientSocket;
		private int localhostServerPort;
		private Socket mServerSocket;
		private boolean mForwardingActive = true;

		public ClientThread(Socket clientSocket, int localhostServerPort) {
			mClientSocket = clientSocket;
			this.localhostServerPort = localhostServerPort;
		}

		/**
		 * Establishes connection to the destination server and starts
		 * bidirectional forwarding ot data between the client and the server.
		 */
		public void run() {
			InputStream clientIn;
			OutputStream clientOut;
			InputStream serverIn;
			OutputStream serverOut;
			try {
				// Connect to the destination server
				mServerSocket = new Socket("localhost", localhostServerPort);
				mServerSocket.setSoTimeout(0);

				// // Turn on keep-alive for both the sockets
				mServerSocket.setKeepAlive(true);
				mClientSocket.setKeepAlive(true);

				// Obtain client & server input & output streams
				clientIn = mClientSocket.getInputStream();
				clientOut = mClientSocket.getOutputStream();
				serverIn = mServerSocket.getInputStream();
				serverOut = mServerSocket.getOutputStream();
			} catch (IOException ioe) {
				System.err.println("Can not connect to " + "localhost" + ":"
						+ localhostServerPort);
				connectionBroken();
				return;
			}

			// Start forwarding data between server and client
			mForwardingActive = true;
			ForwardThread clientForward = new ForwardThread(this, clientIn,
					serverOut, true);
			clientForward.start();
			ForwardThread serverForward = new ForwardThread(this, serverIn,
					clientOut, false);
			serverForward.start();

		}

		/**
		 * Called by some of the forwarding threads to indicate that its socket
		 * connection is brokean and both client and server sockets should be
		 * closed. Closing the client and server sockets causes all threads
		 * blocked on reading or writing to these sockets to get an exception
		 * and to finish their execution.
		 */
		public synchronized void connectionBroken() {
			logger.debug("Closing forwarding socket");
			try {
				mServerSocket.close();
			} catch (Exception e) {
			}
			try {
				mClientSocket.close();
			} catch (Exception e) {
			}

			if (mForwardingActive) {
				mForwardingActive = false;
			}
		}
	}

	/**
	 * ForwardThread handles the TCP forwarding between a socket input stream
	 * (source) and a socket output stream (dest). It reads the input stream and
	 * forwards everything to the output stream. If some of the streams fails,
	 * the forwarding stops and the parent is notified to close all its sockets.
	 */
	class ForwardThread extends Thread {
		private static final int BUFFER_SIZE = 8192;

		InputStream mInputStream;
		OutputStream mOutputStream;
		ClientThread mParent;

		private boolean isClient;

		/**
		 * Creates a new traffic redirection thread specifying its parent, input
		 * stream and output stream.
		 */
		public ForwardThread(ClientThread aParent, InputStream aInputStream,
				OutputStream aOutputStream, boolean isClient) {
			mParent = aParent;
			mInputStream = aInputStream;
			mOutputStream = aOutputStream;
			this.isClient = isClient;
		}

		/**
		 * Runs the thread. Continuously reads the input stream and writes the
		 * read data to the output stream. If reading or writing fail, exits the
		 * thread and notifies the parent about the failure.
		 */
		public void run() {
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = -1;

			while (true) {
				try {
					bytesRead = mInputStream.read(buffer);
					if (bytesRead == -1)
						break; // End of stream is reached --> exit

					mOutputStream.write(buffer, 0, bytesRead);
					mOutputStream.flush();
				} catch (IOException e) {

				}

			}

			// Notify parent thread that the connection is broken
			mParent.connectionBroken();
		}
	}
}
