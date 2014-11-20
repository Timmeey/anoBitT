package de.timmeey.anoBitT.config;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timmeeyLib.exceptions.unchecked.NotYetInitializedException;
import timmeeyLib.properties.PropertiesAccessor;
import timmeeyLib.properties.PropertiesFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.sun.net.httpserver.HttpServer;

import de.timmeey.anoBitT.communication.HTTPRequestService;
import de.timmeey.anoBitT.communication.external.ExternalCommunicationHandler;
import de.timmeey.anoBitT.communication.impl.HTTPRequestHandlerImpl;
import de.timmeey.anoBitT.config.GuiceAnnotations.AppProperties;
import de.timmeey.anoBitT.config.GuiceAnnotations.DHTProperties;
import de.timmeey.anoBitT.config.GuiceAnnotations.HTTPRequestExecutor;
import de.timmeey.anoBitT.config.GuiceAnnotations.TorProperties;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.dht.impl.DHTServiceFakeImpl;
import de.timmeey.anoBitT.network.SocketFactory;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.anoBitT.tor.TorManager;

public class AnonBitTModule extends AbstractModule {

	private final ExecutorService httpExecutor = new ThreadPoolExecutor(5, 30,
			60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(65));

	@Override
	protected void configure() {
		try {

			bind(DHTService.class).to(DHTServiceFakeImpl.class);

			bind(PropertiesAccessor.class).annotatedWith(TorProperties.class)
					.toInstance(PropertiesFactory.getPropertiesAccessor("tor"));

			bind(PropertiesAccessor.class).annotatedWith(AppProperties.class)
					.toInstance(PropertiesFactory.getPropertiesAccessor("app"));

			bind(SocketFactory.class);

			bind(TorManager.class);

			bind(HTTPRequestService.class).to(HTTPRequestHandlerImpl.class);

			bind(ExecutorService.class)
					.annotatedWith(HTTPRequestExecutor.class).toInstance(
							httpExecutor);

			bind(ExternalCommunicationHandler.class);
			bind(HttpServer.class).toInstance(HttpServer.create());

			bind(PropertiesAccessor.class).annotatedWith(DHTProperties.class)
					.toInstance(PropertiesFactory.getPropertiesAccessor("dht"));

			bind(DHTService.class).to(DHTServiceFakeImpl.class);

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
