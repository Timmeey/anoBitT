package anoBitT;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Iterator;

import timmeeyLib.properties.PropertiesFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;

import de.timmeey.anoBitT.communication.HTTPRequestService;
import de.timmeey.anoBitT.communication.httpServer.HttpContext;
import de.timmeey.anoBitT.communication.httpServer.HttpHandler;
import de.timmeey.anoBitT.communication.httpServer.TimmeeyHttpSimpleServer;
import de.timmeey.anoBitT.config.AnonBitTModule;
import de.timmeey.anoBitT.config.DefaultsConfigModule;
import de.timmeey.anoBitT.config.GuiceAnnotations.DHTExternalPort;
import de.timmeey.anoBitT.network.SocketFactory;
import de.timmeey.anoBitT.tor.TorManager;

public class DHTServer {
	private static HashMap<String, String> dht = new HashMap<String, String>();
	private static HashMap<String, Long> dhtTMO = new HashMap<String, Long>();
	private static final Object readLock = new Object();
	private static final Object writeLock = new Object();

	private static final long minRetention = 1000 * 60 * 2; // 1 hour
	private static final long cleanupPeriod = 1000 * 60 * 1; // 1 Minute

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

		SocketFactory socketFactory = injector.getInstance(SocketFactory.class);

		int dhtExternalPort = injector.getInstance(Key.get(Integer.class,
				DHTExternalPort.class));

		TimmeeyHttpSimpleServer server = injector
				.getInstance(TimmeeyHttpSimpleServer.class);
		ServerSocket serverSocket = socketFactory
				.createTorServerSocket(dhtExternalPort);
		server.setServerSocket(serverSocket);
		System.out.println("ready");

		DHTGetRequest.addHandler(server, new HttpHandler() {

			@Override
			public HttpContext handle(HttpContext ctx) {
				System.out.println("Get request");
				DHTGetRequest request = ctx.getPayload(DHTGetRequest.class);
				String value = getValue(request.getKey());

				ctx.setResponse(new DHTReply(request.getKey(), value));
				return ctx;

			}
		});

		DHTPutRequest.addHandler(server, new HttpHandler() {

			@Override
			public HttpContext handle(HttpContext ctx) {
				System.out.println("Put request");
				DHTPutRequest put = ctx.getPayload(DHTPutRequest.class);
				putValue(put.getKey(), put.getValue());

				ctx.setResponse(new DHTReply(put.getKey(), put.getValue()));
				return ctx;

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
		};// .run();

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
				System.out.println("Cleanup");
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
				System.out.println("Deleted: " + deleted);
				Thread.sleep(1000);
			}
		}

	}
}
