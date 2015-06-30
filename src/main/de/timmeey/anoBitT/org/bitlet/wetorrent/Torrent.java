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

package de.timmeey.anoBitT.org.bitlet.wetorrent;

import static de.timmeey.anoBitT.org.bitlet.wetorrent.util.Utils.toByteBuffer;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.org.bitlet.wetorrent.choker.Choker;
import de.timmeey.anoBitT.org.bitlet.wetorrent.disk.TorrentDisk;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.IncomingPeerListener;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.Peer;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.PeersManager;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.message.Have;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.message.Request;
import de.timmeey.anoBitT.org.bitlet.wetorrent.pieceChooser.PieceChooser;
import de.timmeey.anoBitT.org.bitlet.wetorrent.pieceChooser.RouletteWheelPieceChooser;
import de.timmeey.anoBitT.org.bitlet.wetorrent.util.stream.BandwidthLimiter;
import de.timmeey.anoBitT.org.bitlet.wetorrent.util.thread.InterruptableTasksThread;
import de.timmeey.anoBitT.torrent.MinimalPeer;
import de.timmeey.libTimmeey.networking.SocketFactory;
import static com.google.common.base.Preconditions.checkNotNull;

public class Torrent extends InterruptableTasksThread {

	private final static Logger logger = LoggerFactory.getLogger(Torrent.class);

	public static final short maxUnfulfilledRequestNumber = 6;
	private Metafile metafile;
	String name;
	private String peerIdEncoded;
	private int port;
	private Tracker activeTracker = null;
	private PeersManager peersManager;
	private TorrentDisk torrentDisk;
	private IncomingPeerListener incomingPeerListener;
	private PieceChooser pieceChooser = null;
	private Choker choker = new Choker(this);
	public static final String agent = "BitLet.org/0.1";
	public static final boolean verbose = true;
	private BandwidthLimiter uploadBandwidthLimiter;

	private boolean stopped = false;

	public BandwidthLimiter getUploadBandwidthLimiter() {
		return uploadBandwidthLimiter;
	}

	public Torrent(Metafile metafile, TorrentDisk torrentDisk,
			IncomingPeerListener incomingPeerListener, Tracker tracker,
			SocketFactory torSocketFactory, SocketFactory plainSocketFactory) {
		this(metafile, torrentDisk, incomingPeerListener, null, tracker,
				torSocketFactory, plainSocketFactory);
	}

	public Torrent(Metafile metafile, TorrentDisk torrentDisk,
			IncomingPeerListener incomingPeerListener,
			BandwidthLimiter uploadBandwidthLimiter, Tracker tracker,
			SocketFactory torSocketFactory, SocketFactory plainSocketFactory) {

		this.uploadBandwidthLimiter = uploadBandwidthLimiter;
		this.metafile = metafile;
		this.torrentDisk = torrentDisk;

		this.pieceChooser = new RouletteWheelPieceChooser();

		this.pieceChooser.setTorrent(this);

		this.incomingPeerListener = checkNotNull(incomingPeerListener);
		this.port = incomingPeerListener.getPort();
		incomingPeerListener.register(this);
		checkNotNull(plainSocketFactory);
		checkNotNull(torSocketFactory);

		this.peersManager = new PeersManager(this, plainSocketFactory,
				torSocketFactory, this.port);

		activeTracker = checkNotNull(tracker);

	}

	public synchronized void addEvent(Event event) {
		if (event.getLevel() == Level.FINEST) {
			logger.trace((event.getDescription() + ": " + event.getAuthor()));
		} else if (event.getLevel() == Level.WARNING) {
			logger.warn((event.getDescription() + ": " + event.getAuthor()));
		} else {

			logger.debug((event.getDescription() + ": " + event.getAuthor()));
		}
		/* events.add(event); */
	}

	public Metafile getMetafile() {
		return metafile;
	}

	public TorrentDisk getTorrentDisk() {
		return torrentDisk;
	}

	public byte[] getPeerId() {
		return activeTracker.getPeerId();
	}

	public String getPeerIdEncoded() {
		return peerIdEncoded;
	}

	public PeersManager getPeersManager() {
		return peersManager;
	}

	// This function notifies that peer has just sent an amInterested message
	public void have(int index, Peer peer) {

		if (!torrentDisk.isCompleted(index)) {
			peer.setAmInterested(true);
			if (!peer.isAmChoked()) {
				addRequests(peer);
			}
		}

	}

	public void bitfield(byte[] bitfield, Peer peer) {

		for (int i = 0; i < metafile.getPieces().size(); i++) {

			if (peer.hasPiece(i) && !torrentDisk.isCompleted(i)) {
				peer.setAmInterested(true);
				return;
			}
		}
	}

	public void choke(Peer peer) {
		choker.choke(peer);
		pieceChooser.interrupted(peer);
	}

	public void unchoke(Peer peer) {
		/* remove all the pending request */
		pieceChooser.interrupted(peer);
		choker.unchoke(peer);
		addRequests(peer);
	}

	public void interested(Peer peer) {
		choker.interested(peer);
	}

	public void notInterested(Peer peer) {
		choker.notInterested(peer);
	}

	public void piece(int index, int begin, byte[] block, Peer peer) {

		try {
			torrentDisk.write(index, begin, block);
			pieceChooser.piece(index, begin, block, peer);
		} catch (Exception e) {
			if (Torrent.verbose) {
				addEvent(new Event(e, "Exception writing piece", Level.SEVERE));
			}
			e.printStackTrace(System.err);
		}

		if (Torrent.verbose) {
			addEvent(new Event(
					peer,
					"PIECE "
							+ index
							+ " "
							+ ((float) torrentDisk.getDownloaded(index) / torrentDisk
									.getLength(index)), Level.FINEST));
		}
		if (torrentDisk.isCompleted(index)) {
			peersManager.sendHave(new Have(index));
		}

		addRequests(peer);

	}

	public void interrupted(Peer peer) {
		choker.interrupted(peer);
		pieceChooser.interrupted(peer);
	}

	private void addRequests(Peer peer) {
		Request request = null;

		int[] piecesFrequencies = peersManager.getPiecesFrequencies();

		while (peer.getUnfulfilledRequestNumber() < maxUnfulfilledRequestNumber
				&& (request = pieceChooser.getNextBlockRequest(peer,
						piecesFrequencies)) != null) {
			peer.sendMessage(request);
		}
		if (request == null && peer.getUnfulfilledRequestNumber() == 0) {
			peer.setAmInterested(false);
		}
	}

	public void addPeers(Set<MinimalPeer> set) throws UnknownHostException {
		for (MinimalPeer elem : set) {
			checkNotNull(elem);
			logger.debug("Processing peer: {}", elem.getAddress());

			if (Torrent.verbose) {
				addEvent(new Event(this, "Offering new peer: "
						+ elem.getAddress(), Level.FINE));
			}

			peersManager.offer(elem.getPeerID(), elem.getAddress());
		}

	}

	public boolean isCompleted() {
		return torrentDisk.getCompleted() == metafile.getLength();
	}

	public void tick() {
		tick(false);
	}

	public void tick(boolean force) {

		peersManager.tick();
		choker.tick();

		if (!isCompleted()) {
			long now = System.currentTimeMillis();
			if (force
					|| now - activeTracker.getLastRequestTime() >= activeTracker
							.getREQUEST_INTERVAL()) {

				if (!stopped) {
					try {
						addPeers(activeTracker.getMorePeers(this));

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public IncomingPeerListener getIncomingPeerListener() {
		return incomingPeerListener;
	}

	public void stopDownload() {
		stopped = true;
		this.activeTracker.stop(this);
		System.out.println("Torrent was stopped, interrupting everything");

		getPeersManager().interrupt();
	}

	public void startDownload() throws Exception {
		logger.info("Starting torrent {}", this.getName());
		stopped = false;
		activeTracker.start(this);
		this.tick();
	}
}
