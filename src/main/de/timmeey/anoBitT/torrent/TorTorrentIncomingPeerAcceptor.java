package de.timmeey.anoBitT.torrent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.main;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.IncomingPeerListener;
import de.timmeey.libTimmeey.networking.SocketFactory;

public class TorTorrentIncomingPeerAcceptor extends Thread {
	private final IncomingPeerListener incomingListener;
	private final SocketFactory socketFactory;
	private int port;
	ServerSocket serverSocket;
	private static final Logger logger = LoggerFactory
			.getLogger(TorTorrentIncomingPeerAcceptor.class);
	public static List<SocketTransferRateWrapper> sockets = new ArrayList<>();

	public TorTorrentIncomingPeerAcceptor(
			IncomingPeerListener incomingListener, SocketFactory socketFactory,
			int port) throws IOException {
		this.incomingListener = incomingListener;
		this.socketFactory = socketFactory;
		this.port = port;

	}

	@Override
	public void run() {
		try {
			serverSocket = socketFactory.getServerSocket(port);
			logger.info(
					"Opened tor ServerSocket on port {} to accept incoming torrent connections from anyone",
					port);
			while (!Thread.interrupted()) {
				try {
					Socket client = serverSocket.accept();
					// SocketTransferRateWrapper cl = new
					// SocketTransferRateWrapper(serverSocket.accept());
					// sockets.add(cl);
					logger.error("Woho, got a tor torrent connection, dispatching");
					incomingListener.addConnection(client);

				} catch (IOException e) {
					logger.error("Got IOxception from a TOR torrent socket");
				}
			}
		} catch (IOException e) {
			logger.error(
					"Got IOException on TOR ServerSocket listening on port {} for torrent requests",
					port, e);
			main.emergencyShutdown(
					"Got IOException on TOR ServerSocket listening for torrent requests",
					e);
		}

	}
}
