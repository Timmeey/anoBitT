package de.timmeey.anoBitT.peerGroup.management;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.peerGroup.PeerGroup;
import de.timmeey.anoBitT.tor.KeyPair;
import timmeeyLib.properties.PropertiesAccessor;

@Singleton
public class PeerGroupApplicationManager {
	private final DHTService dhtService;
	private final PropertiesAccessor props;
	private final List<PeerGroupApplicationOffer> offers = new ArrayList<PeerGroupApplicationOffer>();
	private final KeyPair keyPair;

	@Inject
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
