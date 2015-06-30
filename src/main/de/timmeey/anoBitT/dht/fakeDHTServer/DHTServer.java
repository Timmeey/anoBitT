package de.timmeey.anoBitT.dht.fakeDHTServer;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.gson.Gson;

import de.timmeey.anoBitT.main;
import de.timmeey.anoBitT.network.impl.SocketFactoryImpl;
import de.timmeey.anoBitT.tor.TorManager;
import de.timmeey.anoBitT.util.GsonSerializer;
import de.timmeey.libTimmeey.networking.NetSerializer;
import de.timmeey.libTimmeey.networking.SocketFactory;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPFilter;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HTTPResponse.ResponseCode;
import de.timmeey.libTimmeey.networking.communicationServer.HttpContext;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;
import de.timmeey.libTimmeey.properties.PropertiesFactory;

public class DHTServer {
	private static final Logger logger = LoggerFactory
			.getLogger(DHTServer.class);
	private static HashMap<String, List<String>> dht = new HashMap<String, List<String>>();
	private static HashMap<String, Long> dhtTMO = new HashMap<String, Long>();
	private static final Object readLock = new Object();
	private static final Object writeLock = new Object();

	private static final long minRetention = 1000 * 60 * 2; // 1 hour
	private static final long cleanupPeriod = 1000 * 60 * 1; // 1 Minute

	private static final NetSerializer gson = new GsonSerializer();

	public static void startDHTServer(TorManager tor,
			PropertiesAccessor dhtProps) throws Exception {
		Preconditions.checkNotNull(tor);
		Preconditions.checkNotNull(dhtProps);

		logger.info("Starting System");

		SocketFactory socketFactory = tor.getTorSocketFactory();
		// SocketFactory socketFactory = new SocketFactoryImpl();
		int DHTPort = Integer.parseInt(dhtProps.getProperty("DHTPort"));
		ServerSocket serverSocket = socketFactory.getServerSocket(DHTPort);

		TimmeeyHttpSimpleServer server = new TimmeeyHttpSimpleServer(gson,
				serverSocket);
		server.startServer();
		logger.info("DHTServer ready");

		server.registerFilter(new HTTPFilter() {

			@Override
			public boolean doFilter(String path, HttpContext ctx) {
				logger.error("AuthMap: {}", ctx.getPayload(HTTPRequest.class)
						.getAuthenticationMap());
				logger.trace("I'm a filter");
				return true;
			}
		});

		DHTGetRequest.addHandler(server, new HttpHandler() {

			@Override
			public HttpContext handle(HttpContext ctx) {
				logger.trace("Get request");
				DHTGetRequest request = ctx.getPayload(DHTGetRequest.class);
				List<String> value = getValue(request.getKey());
				ctx.setResponse(new DHTReply(request.getKey(), value));
				ctx.setResponseCode(ResponseCode.SUCCESS);
				return ctx;

			}
		});

		DHTPutRequest.addHandler(server, new HttpHandler() {

			@Override
			public HttpContext handle(HttpContext ctx) {
				logger.trace("Put request");
				DHTPutRequest put = ctx.getPayload(DHTPutRequest.class);
				putValue(put.getKey(), put.getValue());
				ctx.setResponse(new DHTReply(put.getKey(),
						dht.get(put.getKey())));
				ctx.setResponseCode(ResponseCode.SUCCESS);
				return ctx;

			}
		});

		main.addMaintenanceTask(new Callable() {

			public Object call() throws InterruptedException {

				cleanup();
				return null;

			}

		});

		logger.info("DHTServer Running");

	}

	public static void putValue(String key, String value) {

		synchronized (writeLock) {
			logger.trace("putting " + value);
			List<String> previous = dht.get(key);
			if (previous != null) {
				previous.add(value);
				dht.put(key, previous);
			} else {
				previous = new ArrayList<String>();
				previous.add(value);

				dht.put(key, previous);

			}
			dhtTMO.put(key, System.currentTimeMillis());
		}

	}

	public static List<String> getValue(String key) {
		synchronized (readLock) {
			List<String> value = dht.get(key);
			logger.trace("Reading " + key);
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
		return;
		// synchronized (writeLock) {
		// synchronized (readLock) {
		// Stopwatch stop = Stopwatch.createStarted();
		// int deleted = 0;
		// logger.trace("Cleanup started");
		// long now = System.currentTimeMillis();
		// for (Iterator iterator = dht.keySet().iterator(); iterator
		// .hasNext();) {
		// String key = (String) iterator.next();
		// long creationTime = dhtTMO.get(key);
		// if (creationTime != 0 && creationTime + minRetention < now) {
		// iterator.remove();
		// dhtTMO.remove(key);
		// deleted++;
		// }
		//
		// }
		// stop.stop();
		// logger.debug("Cleanup completed. Took {} and deleted: {}",
		// stop.toString(), deleted);
		//
		// }
		// }

	}
}
