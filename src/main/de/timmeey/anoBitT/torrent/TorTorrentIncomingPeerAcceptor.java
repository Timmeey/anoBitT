package de.timmeey.anoBitT.torrent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.main;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.IncomingPeerListener;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.libTimmeey.networking.SocketFactory;

public class TorTorrentIncomingPeerAcceptor extends Thread {
	private final IncomingPeerListener incomingListener;
	private final SocketFactory socketFactory;
	private int port;
	private static final Logger logger = LoggerFactory
			.getLogger(TorTorrentIncomingPeerAcceptor.class);

	public TorTorrentIncomingPeerAcceptor(
			IncomingPeerListener incomingListener, SocketFactory socketFactory,
			int port) {
		this.incomingListener = incomingListener;
		this.socketFactory = socketFactory;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			ServerSocket s = socketFactory.getServerSocket(port);
			logger.info(
					"Opened tor ServerSocket on port {} to accept incoming torrent connections from anyone",
					port);
			while (!Thread.interrupted()) {
				try {
					Socket c = s.accept();
					logger.debug("Woho, got a tor torrent connection, dispatching");
					incomingListener.addConnection(c);

				} catch (IOException e) {
					logger.debug("Got IOxception from a TOR torrent socket");
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
