package anoBitT;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import timmeeyLib.properties.PropertiesAccessor;
import timmeeyLib.properties.PropertiesFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.timmeey.anoBitT.communication.HTTPRequestService;
import de.timmeey.anoBitT.communication.external.ExternalCommunicationHandler;
import de.timmeey.anoBitT.config.AnonBitTModule;
import de.timmeey.anoBitT.config.DefaultsConfigModule;
import de.timmeey.anoBitT.config.GuiceAnnotations.DHTExternalPort;
import de.timmeey.anoBitT.config.GuiceAnnotations.DHTProperties;
import de.timmeey.anoBitT.network.SocketFactory;
import de.timmeey.anoBitT.tor.TorManager;

public class DHTServer {
	private static HashMap<String, String> dht = new HashMap<String, String>();
	private static HashMap<String, Long> dhtTMO = new HashMap<String, Long>();
	private static final Object readLock = new Object();
	private static final Object writeLock = new Object();

	private static final long minRetention = 5000; // 1 hour
	private static final long cleanupPeriod = 1000; // 1 Minute

	public static void main(String[] args) throws Exception {
		PropertiesFactory.setConfDir("anonBit");

		System.out.println("Starting");
		Injector injector = Guice.createInjector(new AnonBitTModule(),
				new DHTFakeServiceServerModule(), new DefaultsConfigModule());

		/*
		 * Now that we've got the injector, we can build objects.
		 */
		TorManager tor = injector.getInstance(TorManager.class);
		System.out.println("Startin tor");
		tor.startTor();
		System.out.println("Tor started");
		HTTPRequestService serializerService = injector
				.getInstance(HTTPRequestService.class);

		SocketFactory socketFactory = injector.getInstance(SocketFactory.class);

		HttpServer dhtFakeServiceServer = injector
				.getInstance(HttpServer.class);
		int dhtExternalPort = injector.getInstance(Key.get(Integer.class,
				DHTExternalPort.class));

		dhtFakeServiceServer.bind(new InetSocketAddress("localhost",
				dhtExternalPort - 1), 0);
		socketFactory.createTorServerSocketToLocalServerSocketForward(
				dhtExternalPort, dhtExternalPort - 1);
		dhtFakeServiceServer.start();
		DHTGetRequest.addHandler(dhtFakeServiceServer, new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {
				System.out.println("get request");
				DHTGetRequest request = serializerService.deserializeRequest(
						exchange.getRequestBody(), DHTGetRequest.class);
				String value = getValue(request.getKey());

				String serialized = serializerService
						.serializeHTTPResponse(new DHTReply(request.getKey(),
								value));
				System.out.println("Writing get output " + serialized);
				exchange.sendResponseHeaders(200, serialized.length());
				OutputStreamWriter writer = new OutputStreamWriter(exchange
						.getResponseBody());
				writer.write(serialized);
				writer.flush();
				writer.close();
				// exchange.close();
			}
		});

		DHTPutRequest.addHandler(dhtFakeServiceServer, new HttpHandler() {

			@Override
			public void handle(HttpExchange exchange) throws IOException {
				System.out.println("Handle put");
				DHTPutRequest request = serializerService.deserializeRequest(
						exchange.getRequestBody(), DHTPutRequest.class);
				putValue(request.getKey(), request.getValue());

				String serialized = serializerService
						.serializeHTTPResponse(new DHTReply(request.getKey(),
								request.getValue()));
				System.out.println("writing put output");
				new OutputStreamWriter(exchange.getResponseBody())
						.write(serialized);
				exchange.close();
			}
		});

		new Runnable() {

			public void run() {
				while (!Thread.interrupted()) {
					// cleanup every hour
					try {
						Thread.sleep(cleanupPeriod);
						cleanup();
					} catch (InterruptedException e) {
						System.out.println("interrupted");
						e.printStackTrace();
					}

				}

			}
		}.run();

	}

	public static void putValue(String key, String value) {

		synchronized (writeLock) {
			System.out.println("putting " + value);
			dht.put(key, value);
			dhtTMO.put(key, System.currentTimeMillis());
		}

	}

	public static String getValue(String key) {
		synchronized (readLock) {
			String value = dht.get(key);
			System.out.println("Reading " + key);
			dhtTMO.put(key, System.currentTimeMillis());
			return value;
		}

	}

	/**
	 * Cleans the hashmap periodically
	 * 
	 * @throws InterruptedException
	 */
	private static void cleanup() throws InterruptedException {
		synchronized (writeLock) {
			synchronized (readLock) {
				int deleted = 0;
				// System.out.println("Cleanup");
				long now = System.currentTimeMillis();
				for (Iterator iterator = dht.keySet().iterator(); iterator
						.hasNext();) {
					String key = (String) iterator.next();
					long creationTime = dhtTMO.get(key);
					if (creationTime != 0 && creationTime + minRetention < now) {
						iterator.remove();
						dhtTMO.remove(key);
						deleted++;
					}

				}
				// System.out.println("Deleted: " + deleted);
				Thread.sleep(1000);
			}
		}

	}
}
