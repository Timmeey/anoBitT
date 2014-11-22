package de.timmeey.anoBitT.config;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timmeeyLib.exceptions.unchecked.NotYetInitializedException;
import timmeeyLib.networking.NetSerializer;
import timmeeyLib.networking.SocketFactory;
import timmeeyLib.networking.communicationClient.HTTPRequestHandlerImpl;
import timmeeyLib.networking.communicationClient.HTTPRequestService;
import timmeeyLib.networking.communicationServer.TimmeeyHttpSimpleServer;
import timmeeyLib.pooling.ObjectPool;
import timmeeyLib.pooling.SimpleObjectPool;
import timmeeyLib.pooling.Verifier;
import timmeeyLib.properties.PropertiesAccessor;
import timmeeyLib.properties.PropertiesFactory;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import de.timmeey.anoBitT.communication.NetSerializerImpl;
import de.timmeey.anoBitT.communication.external.ExternalCommunicationHandler;
import de.timmeey.anoBitT.config.GuiceAnnotations.AnonSocketFactory;
import de.timmeey.anoBitT.config.GuiceAnnotations.AppProperties;
import de.timmeey.anoBitT.config.GuiceAnnotations.DHTProperties;
import de.timmeey.anoBitT.config.GuiceAnnotations.ExternalHTTPRequestExecutor;
import de.timmeey.anoBitT.config.GuiceAnnotations.ExternalHTTPRequestService;
import de.timmeey.anoBitT.config.GuiceAnnotations.ExternalHTTPRequestSocketPool;
import de.timmeey.anoBitT.config.GuiceAnnotations.InternalHTTPRequestExecutor;
import de.timmeey.anoBitT.config.GuiceAnnotations.InternalHTTPRequestService;
import de.timmeey.anoBitT.config.GuiceAnnotations.InternalHTTPRequestSocketPool;
import de.timmeey.anoBitT.config.GuiceAnnotations.NonAnonSocketFactory;
import de.timmeey.anoBitT.config.GuiceAnnotations.TorProperties;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.dht.impl.DHTServiceFakeImpl;
import de.timmeey.anoBitT.network.impl.AnonSocketFactoryImpl;
import de.timmeey.anoBitT.network.impl.SocketFactoryImpl;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.anoBitT.tor.TorManager;

public class AnonBitTModule extends AbstractModule {

	private final long TORSOCKETIDLETIMOUT = 1000 * 60 * 15; // 15min
	private final long TORSOCKETPOOLCLEANUPTIME = 1000 * 60 * 5; // 5 min

	@Override
	protected void configure() {
		try {

			bind(PropertiesAccessor.class).annotatedWith(TorProperties.class)
					.toInstance(PropertiesFactory.getPropertiesAccessor("tor"));

			bind(PropertiesAccessor.class).annotatedWith(AppProperties.class)
					.toInstance(PropertiesFactory.getPropertiesAccessor("app"));

			bind(PropertiesAccessor.class).annotatedWith(DHTProperties.class)
					.toInstance(PropertiesFactory.getPropertiesAccessor("dht"));

			bind(SocketFactory.class).annotatedWith(AnonSocketFactory.class)
					.to(AnonSocketFactoryImpl.class).asEagerSingleton();
			;
			bind(SocketFactory.class).annotatedWith(NonAnonSocketFactory.class)
					.to(SocketFactoryImpl.class).asEagerSingleton();
			;

			bind(TorManager.class).asEagerSingleton();
			;

			bind(NetSerializer.class).to(NetSerializerImpl.class)
					.asEagerSingleton();
			;

			bind(ExternalCommunicationHandler.class);

			bind(DHTService.class).to(DHTServiceFakeImpl.class)
					.asEagerSingleton();
			;

			bind(Gson.class).asEagerSingleton();
			;

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

	@Provides
	@ExternalHTTPRequestService
	HTTPRequestService provideExternalHTTPRequestService(
			@NonAnonSocketFactory SocketFactory socketFactory,
			NetSerializer gson,
			@ExternalHTTPRequestExecutor ExecutorService execService,
			@ExternalHTTPRequestSocketPool ObjectPool<String, Socket> socketPool) {
		HTTPRequestService service = new HTTPRequestHandlerImpl(socketFactory,
				gson, execService, socketPool);
		return service;
	}

	@Provides
	@InternalHTTPRequestService
	HTTPRequestService provideInternalHTTPRequestService(
			@NonAnonSocketFactory SocketFactory socketFactory,
			NetSerializer gson,
			@InternalHTTPRequestExecutor ExecutorService execService,
			@InternalHTTPRequestSocketPool ObjectPool<String, Socket> socketPool) {
		HTTPRequestService service = new HTTPRequestHandlerImpl(socketFactory,
				gson, execService, socketPool);
		return service;
	}

	@Provides
	TimmeeyHttpSimpleServer provideTimmeeyHttpSimpleServer(NetSerializer serial) {
		return new TimmeeyHttpSimpleServer(serial);
	}

	@Provides
	@ExternalHTTPRequestSocketPool
	ObjectPool<String, Socket> provideExternalHTTPRequestSocketObjectPool() {
		Verifier<Socket> verifier = new Verifier<Socket>() {

			@Override
			public boolean verify(Socket object) {
				return !object.isClosed();
			}
		};
		return new SimpleObjectPool<String, Socket>(TORSOCKETIDLETIMOUT,
				TORSOCKETPOOLCLEANUPTIME, verifier);
	}

	@Provides
	@InternalHTTPRequestSocketPool
	ObjectPool<String, Socket> provideInternalHTTPRequestSocketObjectPool() {
		Verifier<Socket> verifier = new Verifier<Socket>() {

			@Override
			public boolean verify(Socket object) {
				return !object.isClosed();
			}
		};
		return new SimpleObjectPool<String, Socket>(TORSOCKETIDLETIMOUT,
				TORSOCKETPOOLCLEANUPTIME, verifier);
	}

	@Provides
	@InternalHTTPRequestExecutor
	@Singleton
	ExecutorService provideInternalExecutorService() {
		return new ThreadPoolExecutor(2, 15, 30, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(150));
	}

	@Provides
	@ExternalHTTPRequestExecutor
	@Singleton
	ExecutorService provideExternalExecutorService() {
		return new ThreadPoolExecutor(5, 30, 60, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>(65));
	}
}
