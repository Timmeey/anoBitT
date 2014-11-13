package de.timmeey.anoBitT.config;

import java.io.IOException;

import timmeeyLib.exceptions.unchecked.NotYetInitializedException;
import timmeeyLib.properties.PropertiesAccessor;
import timmeeyLib.properties.PropertiesFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import de.timmeey.anoBitT.config.GuiceAnnotations.TorProperties;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.dht.DHTServiceImpl;
import de.timmeey.anoBitT.network.portSocketForwarder.SocketFactory;
import de.timmeey.anoBitT.network.portSocketForwarder.UrlFactory;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.anoBitT.tor.TorManager;

public class AnonBitTModule extends AbstractModule {
	@Override
	protected void configure() {
		try {

			bind(DHTService.class).to(DHTServiceImpl.class);

			bind(PropertiesAccessor.class).annotatedWith(TorProperties.class)
					.toInstance(PropertiesFactory.getPropertiesAccessor("tor"));

			bind(SocketFactory.class);

			bind(TorManager.class);

			bind(UrlFactory.class);

		} catch (NotYetInitializedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Provides
	KeyPair provideTorManagerKeyPair(TorManager torManager) {
		return torManager.getKeyPair();
	}
}
