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
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.libTimmeey.networking.SocketFactory;

public class PlainTorrentIncomingPeerAcceptor extends Thread {
	private static final Logger logger = LoggerFactory
			.getLogger(PlainTorrentIncomingPeerAcceptor.class);

	private final IncomingPeerListener incomingListener;
	private final SocketFactory socketFactory;
	private final PeerGroupManager peerGroupManager;
	private final int port;
	public static List<SocketTransferRateWrapper> sockets = new ArrayList<>();

	public PlainTorrentIncomingPeerAcceptor(
			IncomingPeerListener incomingListener, SocketFactory socketFactory,
			PeerGroupManager peerGroupManager, int port) {
		this.incomingListener = incomingListener;
		this.socketFactory = socketFactory;
		this.peerGroupManager = peerGroupManager;
		this.port = port;
	}

	@Override
	public void run() {
		try {
			ServerSocket s = socketFactory.getServerSocket(port);
			logger.info(
					"Opened plain ServerSocket on port {} to accept incoming torrent connections from peerGroupMembers",
					port);
			while (!Thread.interrupted()) {
				try {
					Socket cl = s.accept();
					// SocketTransferRateWrapper cl = new
					// SocketTransferRateWrapper(
					// s.accept());
					// sockets.add(cl);
					logger.debug(
							"Woho, got a torrent connection from {}, verifying now",
							cl.getInetAddress());
					boolean verification = checkConnectionIsFromTrustedGroupMember(cl);
					if (verification) {
						logger.debug(
								"Client {} is a verified peerGroupMember, dispatching socket to torrentLib",
								cl.getInetAddress());
						incomingListener.addConnection(cl);
					} else {
						logger.warn(
								"Client {} is not a known member of any peerGroup, dropping socket",
								cl.getInetAddress());
						cl.close();
					}
				} catch (IOException e) {
					logger.debug("Got IOxception from peerGroupMember torrent socket");
				}
			}

		} catch (IOException e) {
			logger.error(
					"Got IOException on Plain ServerSocket for listening for peerGroupMember torrent requests",
					e);
			main.emergencyShutdown(
					"Got IOException on Plain ServerSocket for listening for peerGroupMember torrent requests",
					e);
		}

	}

	private boolean checkConnectionIsFromTrustedGroupMember(Socket c) {
		boolean result = peerGroupManager.getMemberForIP(c.getInetAddress())
				.isPresent();
		logger.debug("Searched for {} in all Groups and found {}",
				c.getInetAddress(), result);
		return result;

	}

}
