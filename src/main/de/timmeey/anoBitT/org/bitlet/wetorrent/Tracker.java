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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMember;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.anoBitT.torrent.MinimalPeer;
import de.timmeey.libTimmeey.exceptions.checked.SerializerException;
import de.timmeey.libTimmeey.networking.NetSerializer;

public class Tracker {
	private final static Logger logger = LoggerFactory.getLogger(Tracker.class);

	private final DHTService dht;
	public static byte[] peerID = new byte[20];

	private final PeerGroupManager peerGroupManager;

	private final NetSerializer gson;

	private final KeyPair keyPair;

	private final long REQUEST_INTERVAL = 90 * 1000L;

	public long getREQUEST_INTERVAL() {
		return REQUEST_INTERVAL;
	}

	public long getLastRequestTime() {
		return lastRequest;
	}

	private long lastRequest = 0;

	public Tracker(DHTService dht, NetSerializer gson,
			PeerGroupManager peerGroupManager, KeyPair keyPair) {
		this.dht = checkNotNull(dht);
		this.gson = checkNotNull(gson);
		this.peerGroupManager = checkNotNull(peerGroupManager);
		this.keyPair = checkNotNull(keyPair);
		for (int i = 0; i < peerID.length; i++) {
			peerID[i] = keyPair.getOnionAddress().getBytes()[i];
		}
	}

	public Set<MinimalPeer> getMorePeers(Torrent torrent) {
		logger.debug("Geetin more peers for {}", torrent.getName());
		lastRequest = System.currentTimeMillis();
		String infoHash = torrent.getMetafile().getInfoSha1Encoded();
		Set<MinimalPeer> peers = Sets.newHashSet();

		List<String> answer = dht.get(infoHash);
		logger.debug("answer was <{}>, and hat {} entries", answer,
				answer.size());
		if (answer != null) {
			for (String peer : answer) {
				MinimalPeer tmpPeer = gson.fromJson(peer, MinimalPeer.class);
				if (tmpPeer.getAddress().equals(keyPair.getOnionAddress())) {
					logger.trace("Ignoring self as peer");
					// we don't want self connections
					break;
				}
				Optional<PeerGroupMember> member = peerGroupManager
						.getMemberForOnionAddress(tmpPeer.getAddress());

				if (member.isPresent()) {
					logger.debug("Found peerGroupMember for onionaddress {}",
							member.get().getOnionAddress());
					tmpPeer.setAddress(member.get().getIpAddress());

				} else {
					logger.debug(
							"OnionAddress {} ist not a member of any peergroup",
							tmpPeer.getAddress());
				}

				peers.add(tmpPeer);

			}
			logger.trace("Peers <{}>", peers);
			return peers;
		}
		return peers;

	}

	public void stop(Torrent torrent) {
		// Remove me from dht
		return;
	}

	public boolean start(Torrent torrent) throws SerializerException {
		logger.debug("Putting myself into DHT for torret {}", torrent.getName());
		MinimalPeer selfPeer = new MinimalPeer(peerID,
				keyPair.getOnionAddress());
		return dht.put(torrent.getMetafile().getInfoSha1Encoded(),
				gson.toJson(selfPeer), true);

	}

	public byte[] getPeerId() {
		return this.peerID;
	}

}
