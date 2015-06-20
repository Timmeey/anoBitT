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

package de.timmeey.anoBitT.org.bitlet.wetorrent.peer;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.timmeey.anoBitT.org.bitlet.wetorrent.Torrent;
import de.timmeey.anoBitT.org.bitlet.wetorrent.util.thread.InterruptableTasksThread;
import de.timmeey.anoBitT.org.bitlet.wetorrent.util.thread.ThreadTask;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.anoBitT.torrent.PlainTorrentIncomingPeerAcceptor;
import de.timmeey.anoBitT.torrent.TorTorrentIncomingPeerAcceptor;
import de.timmeey.libTimmeey.networking.SocketFactory;

public class IncomingPeerListener {

	ServerSocket serverSocket;
	private Map<ByteBuffer, Torrent> torrents = new HashMap<ByteBuffer, Torrent>();
	private Set<TorrentPeer> dispatchingPeers = new HashSet<TorrentPeer>();
	private int port;
	private int receivedConnection = 0;
	private final PlainTorrentIncomingPeerAcceptor plainAcceptor;
	private final TorTorrentIncomingPeerAcceptor torAcceptor;

	public IncomingPeerListener(SocketFactory torSocketFactory,
			SocketFactory plainSocketFactory,
			PeerGroupManager peerGroupManager, int port) {

		plainAcceptor = new PlainTorrentIncomingPeerAcceptor(this,
				plainSocketFactory, peerGroupManager, port);
		torAcceptor = new TorTorrentIncomingPeerAcceptor(this,
				torSocketFactory, port);

	}

	public IncomingPeerListener addConnection(Socket socket) {
		receivedConnection++;
		TorrentPeer peer = new TorrentPeer(socket, this);
		dispatchingPeers.add(peer);
		peer.start();
		return this;

	}

	public int getPort() {
		return port;
	}

	public int getReceivedConnection() {
		return receivedConnection;
	}

	public synchronized void register(Torrent torrent) {
		torrents.put(ByteBuffer.wrap(torrent.getMetafile().getInfoSha1()),
				torrent);
	}

	public synchronized void unregister(Torrent torrent) {
		torrents.remove(ByteBuffer.wrap(torrent.getMetafile().getInfoSha1()));
	}

	public synchronized void peer(TorrentPeer dispatchingPeer) {
		dispatchingPeers.add(dispatchingPeer);
	}

	public synchronized boolean dispatchPeer(TorrentPeer dispatchingPeer,
			byte[] infoSha1) {
		dispatchingPeers.remove(dispatchingPeer);
		Torrent torrent = torrents.get(ByteBuffer.wrap(infoSha1));
		if (torrent != null) {
			dispatchingPeer.setPeersManager(torrent.getPeersManager());
			torrent.getPeersManager().offer(dispatchingPeer);
			return true;
		} else {
			return false;
		}
	}

	public void interrupt() {
		plainAcceptor.interrupt();
		torAcceptor.interrupt();
		for (TorrentPeer p : dispatchingPeers) {
			p.interrupt();
		}
	}

	public synchronized void removePeer(TorrentPeer peer) {
		dispatchingPeers.remove(peer);
	}
}
