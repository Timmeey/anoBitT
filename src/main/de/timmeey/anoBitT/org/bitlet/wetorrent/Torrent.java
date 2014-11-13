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

//MODIFIED

package de.timmeey.anoBitT.org.bitlet.wetorrent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import de.timmeey.anoBitT.org.bitlet.wetorrent.choker.Choker;
import de.timmeey.anoBitT.org.bitlet.wetorrent.disk.TorrentDisk;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.IncomingPeerListener;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.Peer;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.PeersManager;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.message.Have;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.message.Request;
import de.timmeey.anoBitT.org.bitlet.wetorrent.pieceChooser.PieceChooser;
import de.timmeey.anoBitT.org.bitlet.wetorrent.pieceChooser.RouletteWheelPieceChooser;
import de.timmeey.anoBitT.org.bitlet.wetorrent.util.Utils;
import de.timmeey.anoBitT.org.bitlet.wetorrent.util.stream.BandwidthLimiter;
import de.timmeey.anoBitT.org.bitlet.wetorrent.util.thread.InterruptableTasksThread;

public class Torrent extends InterruptableTasksThread {

    public static final short maxUnfulfilledRequestNumber = 6;
    private Metafile metafile;
    String name;
    private byte[] peerId;
    private String peerIdEncoded;
    private int port;
    private PeersManager peersManager = new PeersManager(this);
    private TorrentDisk torrentDisk;
    private IncomingPeerListener incomingPeerListener;
    private PieceChooser pieceChooser = null;
    private Choker choker = new Choker(this);
    public static final String agent = "BitLet.org/0.1";
    public static final boolean verbose = false;
    private BandwidthLimiter uploadBandwidthLimiter;

    private boolean stopped = false;

    public BandwidthLimiter getUploadBandwidthLimiter() {
        return uploadBandwidthLimiter;
    }

    public Torrent(Metafile metafile, TorrentDisk torrentDisk, IncomingPeerListener incomingPeerListener) {
        this(metafile, torrentDisk, incomingPeerListener, null);
    }

    public Torrent(Metafile metafile, TorrentDisk torrentDisk, IncomingPeerListener incomingPeerListener, BandwidthLimiter uploadBandwidthLimiter) {
        this(metafile, torrentDisk, incomingPeerListener, uploadBandwidthLimiter, null);
    }

    public Torrent(Metafile metafile, TorrentDisk torrentDisk, IncomingPeerListener incomingPeerListener, BandwidthLimiter uploadBandwidthLimiter, PieceChooser pieceChooser) {

        this.uploadBandwidthLimiter = uploadBandwidthLimiter;
        this.incomingPeerListener = incomingPeerListener;
        this.metafile = metafile;
        this.torrentDisk = torrentDisk;

        if (pieceChooser != null) {
            this.pieceChooser = pieceChooser;
        } else {
            this.pieceChooser = new RouletteWheelPieceChooser();
        }
        this.pieceChooser.setTorrent(this);


        peerId = new byte[20];

        Random random = new Random(System.currentTimeMillis());
        
        random.nextBytes(peerId);
        System.arraycopy("-WT-0001".getBytes(), 0, peerId, 0, 8);

        peerIdEncoded = Utils.byteArrayToURLString(peerId);
        if (Torrent.verbose) {
            addEvent(new Event(this, "peerId generated: " + peerIdEncoded, Level.INFO));
        }
        this.incomingPeerListener = incomingPeerListener;
        this.port = incomingPeerListener.getPort();
        incomingPeerListener.register(this);

    }


    public synchronized void addEvent(Event event) {

        System.err.println(event.getDescription() + ": " + event.getAuthor());
    /* events.add(event); */
    }

    public Metafile getMetafile() {
        return metafile;
    }

    public TorrentDisk getTorrentDisk() {
        return torrentDisk;
    }

    public byte[] getPeerId() {
        return peerId;
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
            addEvent(new Event(peer, "PIECE " + index + " " + ((float) torrentDisk.getDownloaded(index) / torrentDisk.getLength(index)), Level.FINEST));
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

        while (peer.getUnfulfilledRequestNumber() < maxUnfulfilledRequestNumber && (request = pieceChooser.getNextBlockRequest(peer, piecesFrequencies)) != null) {
            peer.sendMessage(request);
        }
        if (request == null && peer.getUnfulfilledRequestNumber() == 0) {
            peer.setAmInterested(false);
        }
    }

    public void addPeers(List<Peer> peers) throws UnknownHostException {

            for (Peer peer : peers) {
                InetAddress address = peer.getIp();
                int port = peer.getPort();
                byte[] peerIdByteString = peer.getPeerId();

                if (Torrent.verbose) {
                    addEvent(new Event(this, "Offering new peer: " + address, Level.FINE));
                }
                peersManager.offer(peerIdByteString, address, port);
            }
        
    }

    public boolean isCompleted() {
        return torrentDisk.getCompleted() == metafile.getLength();
    }

    public void tick() {

        peersManager.tick();
        choker.tick();

//Brauche ich vllt noch
//        Long waitTime = activeTracker.getInterval();
//        if (incomingPeerListener.getReceivedConnection() == 0 || peersManager.getActivePeersNumber() < 4) {
//            waitTime = activeTracker.getMinInterval() != null ? activeTracker.getMinInterval() : 60;
//        }
    }

    public IncomingPeerListener getIncomingPeerListener() {
        return incomingPeerListener;
    }

    public void stopDownload() {
        stopped = true;
        //Hier muss dht informiert werden
//        new Thread() {
//
//            public void run() {
//                try {
//                    trackerRequest("stopped");
//                } catch (Exception ex) {
//                }
//            }
//        }.start();

        getPeersManager().interrupt();
    }

    public void startDownload() throws Exception {
        stopped = false;
        //Request peers

    }
}


