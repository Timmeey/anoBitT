package de.timmeey.anoBitT.peerGroup.application;

import static org.junit.Assert.*;

import java.security.KeyPair;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import de.timmeey.anoBitT.dht.DHTService;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationTest {

	// assume there is a class MyDatabase
	@Mock
	DHTService dht;

	@Test
	public void testDhtEntry() {
		String addr = "123456789";

		PeerGroupApplicationOffer offer = new PeerGroupApplicationOffer(dht,
				5L, null, addr);
		String securedAddr = offer.getSecuredOwnOnionAddress();
		String pwHash = offer.getSecretOneTimePasswordHash();

	}
}
