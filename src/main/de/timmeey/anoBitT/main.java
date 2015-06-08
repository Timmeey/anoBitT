package de.timmeey.anoBitT;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.cliche.ShellFactory;
import de.timmeey.anoBitT.client.ConsoleClient;
import de.timmeey.anoBitT.communication.external.ExternalCommunicationHandler;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.dht.fakeDHTServer.DHTServer;
import de.timmeey.anoBitT.dht.impl.DHTServiceFakeImpl;
import de.timmeey.anoBitT.network.impl.SocketFactoryImpl;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.anoBitT.tor.TorManager;
import de.timmeey.anoBitT.util.GsonSerializer;
import de.timmeey.libTimmeey.networking.NetSerializer;
import de.timmeey.libTimmeey.networking.SocketFactory;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestHandlerImpl;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;
import de.timmeey.libTimmeey.pooling.ObjectPool;
import de.timmeey.libTimmeey.pooling.SimpleObjectPool;
import de.timmeey.libTimmeey.pooling.Verifier;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;
import de.timmeey.libTimmeey.properties.PropertiesFactory;

public class main {

	public static final boolean DEV = true;

	private static HTTPRequestService requestService;
	private static KeyPair keyPair;

	private static SocketFactory socketFactory;
	private static SocketFactory torSocketFactory;
	private static TorManager torManager;
	private static PropertiesAccessor appProps;
	private static PropertiesAccessor torProps;
	private static PropertiesAccessor dhtProps;
	private static final NetSerializer gson = new GsonSerializer();
	private static ExternalCommunicationHandler externalCom;
	private static DHTService dhtService;
	private static Timer maintenanceTimer;
	private static PeerGroupManager peerGroupManager;

	private static final List<Runnable> maintenanceTaskList = new ArrayList<Runnable>();

	private static ExecutorService torRequestExecutorService;

	private static final Logger logger = LoggerFactory.getLogger(main.class);

	public static void main(String[] args) throws Exception {
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption("init", false,
				"switch whether to write new config files or not");
		options.addOption("dhtServer", false,
				"switch whether to run as DHTserver");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		initializeConfig();
		if (cmd.hasOption("init")) {
			writeConfigs();
		}

		initializeNetwork();

		if (cmd.hasOption("dhtServer")) {
			logger.debug("Running as DHTServer");
			DHTServer.startDHTServer(torManager, dhtProps);
		} else {
			logger.debug("We are not running as DHT server");
			initializeServer();
			initializeRequestService();
			initializeDHT();
			initializeMaintenance();
			initializePeerGroups();
			initializeConsole(dhtService, peerGroupManager);

			logger.info("Initialization complete");
		}

	}

	private static void initializePeerGroups() {
		peerGroupManager = new PeerGroupManager(keyPair, requestService,
				Integer.parseInt(appProps.getProperty("externalCommPort")));

	}

	@SuppressWarnings("unused")
	private static void initializeConfig() throws IOException {
		logger.debug("Initializeing Configs");
		PropertiesFactory.setConfDir("anonBit");
		if (DEV != true) {
			appProps = PropertiesFactory.getPropertiesAccessor("app");
			dhtProps = PropertiesFactory.getPropertiesAccessor("dht");
			torProps = PropertiesFactory.getPropertiesAccessor("tor");
		} else {
			appProps = PropertiesFactory.getPropertiesAccessor("app");
			dhtProps = PropertiesFactory.getPropertiesAccessor("dht");
			torProps = PropertiesFactory.getPropertiesAccessor("tor");
		}
		logger.trace("Configs initialized");
	}

	private static void initializeNetwork() throws IOException,
			InterruptedException {
		logger.debug("Initializing Network, starting Tor");
		torManager = new TorManager(torProps);
		torManager.startTor();
		keyPair = torManager.getKeyPair();

		socketFactory = new SocketFactoryImpl();
		torSocketFactory = torManager.getTorSocketFactory();
		logger.debug("Network and Tor initialized");

	}

	private static void initializeServer() throws NumberFormatException,
			IOException {
		logger.debug("Initializing Server");
		externalCom = new ExternalCommunicationHandler(gson, appProps,
				torSocketFactory);

		externalCom.startServer(Integer.parseInt(appProps
				.getProperty("externalCommPort")));
		logger.trace("Server initialized");

	}

	private static void initializeRequestService() {
		logger.debug("Initializing Request Service");

		LinkedBlockingQueue<Runnable> requestQueue = new LinkedBlockingQueue<Runnable>();
		ObjectPool<String, Socket> pool = getTorSocketPool();

		addMaintenanceTask(new Runnable() {

			@Override
			public void run() {
				logger.debug("RequestQueue is currently {} long",
						requestQueue.size());
				logger.debug(
						"SocketPool currently holds {} in total, with {} Sockets lend out",
						pool.totalPoolSize(), pool.currentlyLendOutObjects());

			}
		});
		torRequestExecutorService = new ThreadPoolExecutor(5, 30, 60,
				TimeUnit.SECONDS, requestQueue);

		requestService = new HTTPRequestHandlerImpl(torSocketFactory, gson,
				torRequestExecutorService, pool);

		logger.trace("Request Service Initliaized");
	}

	private static void initializeDHT() {
		logger.debug("Initializing DHTservice");
		dhtService = new DHTServiceFakeImpl(dhtProps, requestService);
		logger.trace("DhtService initialized");
	}

	private static ObjectPool<String, Socket> getTorSocketPool() {
		Verifier<Socket> verifier = new Verifier<Socket>() {

			@Override
			public boolean verify(Socket object) {
				return !object.isClosed();
			}
		};
		try {
			return new SimpleObjectPool<String, Socket>(Long.parseLong(appProps
					.getProperty("TORSOCKETIDLETIMOUT")),
					Long.parseLong(appProps
							.getProperty("TORSOCKETPOOLCLEANUPTIME")), verifier);
		} catch (NumberFormatException nfe) {
			logger.error("Could not parse TORSOCKETIDLETIMOUT or TORSOCKETPOOLCLEANUPTIME from COnfig file");
			throw nfe;
		}
	}

	private static void writeConfigs() throws IOException {
		logger.debug("Writing default config values");
		appProps.addProperty("TORSOCKETIDLETIMOUT", (2 * 60 * 1000) + "");
		appProps.addProperty("TORSOCKETPOOLCLEANUPTIME", (10 * 1000) + "");
		appProps.addProperty("externalCommPort", "24361");

		dhtProps.addProperty("DHTPort", "61342");

	}

	private static void initializeMaintenance() {
		maintenanceTimer = new Timer(true);
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				logger.debug("Maintenance run started");
				for (Runnable runnable : maintenanceTaskList) {
					try {
						runnable.run();
					} catch (Exception e) {
						logger.warn("Maintenance callable threw exception: {}",
								e.getMessage());
					}
				}

			}
		};

		maintenanceTimer.schedule(task, 10000L, 10000L);

	}

	private static void addMaintenanceTask(Runnable c) {
		maintenanceTaskList.add(c);

	}

	public static void emergencyShutdown(String reason, Throwable e) {
		try {

			logger.error(
					"Killing the application because an error occured: {}",
					reason);
			if (e != null) {
				logger.error("An {} was provided: {}", e.getClass().toString(),
						e.getMessage());
			}
		} finally {
			System.exit(1);
		}

	}

	private static void initializeConsole(DHTService dht,
			PeerGroupManager peerGroupManager) {

		new Runnable() {

			@Override
			public void run() {
				try {
					ShellFactory.createConsoleShell("hello", "",
							new ConsoleClient(peerGroupManager, dht))
							.commandLoop();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("Error in ClientShell: {}", e.getMessage());
				}

			}
		}.run();

	}
}
