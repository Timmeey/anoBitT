/*
 *              bitlet - Simple bittorrent library
 *  Copyright (C) 2008 Alessandro Bahgat Shehata, Daniele Castagna
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.timmeey.anoBitT.org.bitlet.wetorrent.peer.task;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.ConnectException;
import java.net.Socket;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.org.bitlet.wetorrent.Event;
import de.timmeey.anoBitT.org.bitlet.wetorrent.Torrent;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.TorrentPeer;
import de.timmeey.anoBitT.org.bitlet.wetorrent.util.thread.ThreadTask;
import de.timmeey.anoBitT.torrent.SocketTransferRateWrapper;
import de.timmeey.libTimmeey.networking.SocketFactory;

public class StartConnection implements ThreadTask {

	private final static Logger logger = LoggerFactory
			.getLogger(StartConnection.class);

	boolean interrupted = false;
	private TorrentPeer peer;
	private SocketFactory plainSocketFactory;
	private SocketFactory torSocketFactory;

	public StartConnection(TorrentPeer peer, SocketFactory plainSocketFactory,
			SocketFactory torSocketFactory) {
		this.peer = checkNotNull(peer);
		this.plainSocketFactory = checkNotNull(plainSocketFactory);
		this.torSocketFactory = checkNotNull(torSocketFactory);
	}

	public boolean execute() throws Exception {
		Socket socket = connect(peer.getOutGoingAddress(), peer.getPort());
		peer.setSocket(socket);
		if (socket != null) {
			if (Torrent.verbose) {
				peer.getPeersManager()
						.getTorrent()
						.addEvent(
								new Event(this, "Connected to "
										+ peer.getOutGoingAddress(), Level.FINE));
			}
		} else {
			throw new Exception("Problem connecting to "
					+ peer.getOutGoingAddress());
		}
		return false;

	}

	public void interrupt() {
		interrupted = true;
	}

	public synchronized boolean isInterrupted() {
		return interrupted;
	}

	public synchronized Socket connect(String address, int port)
			throws Exception {
		if (!interrupted) {
			if (address.contains("onion")) {
				logger.debug("Attempting to connect to onion address {}",
						address);
				return // new SocketTransferRateWrapper(
				torSocketFactory.getSocket(address, port);// );
			} else {
				logger.debug("Got plain ip address {}", address);
				return // new SocketTransferRateWrapper(
				plainSocketFactory.getSocket(address, port);// );
			}
		} else {
			throw new Exception("Interrupted before connecting");
		}
	}

	public void exceptionCought(Exception e) {
		if (e instanceof ConnectException) {
			if (Torrent.verbose) {
				peer.getPeersManager()
						.getTorrent()
						.addEvent(
								new Event(this, "Connection refused: "
										+ peer.getOutGoingAddress(), Level.FINE));
			}
		}
		peer.interrupt();
	}
}
