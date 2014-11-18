package de.timmeey.anoBitT.config;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timmeeyLib.exceptions.unchecked.NotYetInitializedException;
import timmeeyLib.properties.PropertiesAccessor;
import timmeeyLib.properties.PropertiesFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import de.timmeey.anoBitT.config.GuiceAnnotations.HTTPRequestExecutor;
import de.timmeey.anoBitT.config.GuiceAnnotations.TorProperties;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.dht.DHTServiceImpl;
import de.timmeey.anoBitT.http.communication.HTTPRequestService;
import de.timmeey.anoBitT.http.communication.impl.HTTPRequestHandlerImpl;
import de.timmeey.anoBitT.network.portSocketForwarder.SocketFactory;
import de.timmeey.anoBitT.network.portSocketForwarder.UrlFactory;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.anoBitT.tor.TorManager;

public class AnonBitTModule extends AbstractModule {

	private final ExecutorService httpExecutor = new ThreadPoolExecutor(5, 30,
			60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(65));

	@Override
	protected void configure() {
		try {

			bind(DHTService.class).to(DHTServiceImpl.class);

			bind(PropertiesAccessor.class).annotatedWith(TorProperties.class)
					.toInstance(PropertiesFactory.getPropertiesAccessor("tor"));

			bind(SocketFactory.class);

			bind(TorManager.class);

			bind(UrlFactory.class);
			bind(HTTPRequestService.class).to(HTTPRequestHandlerImpl.class);
			bind(ExecutorService.class)
					.annotatedWith(HTTPRequestExecutor.class).toInstance(
							httpExecutor);

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
