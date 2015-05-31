package de.timmeey.anoBitT.peerGroup.application;

import java.util.ArrayList;
import java.util.List;

import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.peerGroup.PeerGroup;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;

public class PeerGroupApplicationManager {
	private final DHTService dhtService;
	private final PropertiesAccessor props;
	private final List<PeerGroupApplicationOffer> offers = new ArrayList<PeerGroupApplicationOffer>();
	private final KeyPair keyPair;

	PeerGroupApplicationManager(DHTService dhtService,
			PropertiesAccessor properties, KeyPair keyPair) {
		this.dhtService = dhtService;
		this.props = properties;
		this.keyPair = keyPair;
	}

	public PeerGroupApplicationOffer createApplicationOffer(PeerGroup group) {
		PeerGroupApplicationOffer offer = new PeerGroupApplicationOffer(
				dhtService, Long.parseLong(props.getProperty(
						"applicationOfferDuration", "180")), group,
				keyPair.getOnionAddress());
		offers.add(offer);
		return offer;
	}
}
