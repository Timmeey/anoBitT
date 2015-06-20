package de.timmeey.anoBitT;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asg.cliche.ShellFactory;

import com.google.common.net.InetAddresses;

import de.timmeey.anoBitT.client.ConsoleClient;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.dht.fakeDHTServer.DHTServer;
import de.timmeey.anoBitT.dht.impl.DHTServiceFakeImpl;
import de.timmeey.anoBitT.network.impl.SocketFactoryImpl;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.IncomingPeerListener;
import de.timmeey.anoBitT.org.bitlet.wetorrent.peer.TorrentPeer;
import de.timmeey.anoBitT.peerGroup.PeerGroupApplicationRequest;
import de.timmeey.anoBitT.peerGroup.PeerGroupApplicationRequestHandler;
import de.timmeey.anoBitT.peerGroup.PeerGroupManager;
import de.timmeey.anoBitT.peerGroup.PeerGroupUpdateRequest;
import de.timmeey.anoBitT.peerGroup.PeerGroupUpdateRequestHandler;
import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMemberIpUpdateRequest;
import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMemberIpUpdateRequestHandler;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.anoBitT.tor.TorManager;
import de.timmeey.anoBitT.torrent.PlainTorrentIncomingPeerAcceptor;
import de.timmeey.anoBitT.torrent.TorTorrentIncomingPeerAcceptor;
import de.timmeey.anoBitT.util.GsonSerializer;
import de.timmeey.libTimmeey.networking.NetSerializer;
import de.timmeey.libTimmeey.networking.SocketFactory;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestHandlerImpl;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;
import de.timmeey.libTimmeey.pooling.ObjectPool;
import de.timmeey.libTimmeey.pooling.SimpleObjectPool;
import de.timmeey.libTimmeey.pooling.Verifier;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;
import de.timmeey.libTimmeey.properties.PropertiesFactory;

public class main {
	static {
		System.setProperty("logback.configurationFile",
				"/home/timmeey/.anonBit/logback.xml");
	}

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
	private static TimmeeyHttpSimpleServer externalCom;
	private static DHTService dhtService;
	private static Timer maintenanceTimer;
	private static PeerGroupManager peerGroupManager;
	private static String ipToBeReachedOn;

	private static final List<Callable> maintenanceTaskList = new ArrayList<Callable>();

	private static ExecutorService torRequestExecutorService;

	private static final Logger logger = LoggerFactory.getLogger(main.class);

	public static void main(String[] args) throws Exception {
		registerShutdownHook();
		// create Options object
		Options options = new Options();

		// add t option
		options.addOption("init", false,
				"switch whether to write new config files or not");
		options.addOption("dhtServer", false,
				"switch whether to run as DHTserver");
		options.addOption(
				"ip",
				true,
				"The ipAddress under which this instance is reachable for other trusted instances");
		options.addOption("configName", true,
				"the name under which the config is found");
		options.addOption("help", false, "Help message");

		HelpFormatter formatter = new HelpFormatter();

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		if (cmd.hasOption("help")) {
			formatter.printHelp("anonBit ", options);
			System.exit(0);
		}

		if (cmd.hasOption("configName")) {
			initializeConfig(cmd.getOptionValue("configName"));
		} else {
			initializeConfig(null);
		}
		if (cmd.hasOption("init")) {
			writeConfigs();
		}

		initializeNetwork();
		SocketFactory socketFactory = torManager.getTorSocketFactory();
		// SocketFactory socketFactory = new SocketFactoryImpl();
		int DHTPort = Integer.parseInt(dhtProps.getProperty("DHTPort"));
		ServerSocket serverSocket = socketFactory.getServerSocket(DHTPort);

		initializeMaintenance();

		if (cmd.hasOption("dhtServer")) {
			logger.debug("Running as DHTServer");
			DHTServer.startDHTServer(torManager, dhtProps);
		} else {
			logger.debug("We are not running as DHT server");
			String ip = cmd.getOptionValue("ip");
			if (InetAddresses.isInetAddress(ip)) {
				ipToBeReachedOn = ip;
			} else {
				// Ip address is not valid
				formatter.printHelp("anonBit ", options);
				emergencyShutdown("Entered a not valid Ip address", null);
			}

			initializeServer();
			initializeRequestService();
			initializeDHT();
			initializePeerGroups();
			registerHandler();
			System.out.println(String.format("Will listen on %s",
					keyPair.getOnionAddress(), ipToBeReachedOn));
			initializeConsole(dhtService, peerGroupManager);

			logger.info("Initialization complete");
		}

	}

	private static void initializePeerGroups() {
		peerGroupManager = new PeerGroupManager(keyPair, requestService,
				Integer.parseInt(appProps.getProperty("externalCommPort")),
				ipToBeReachedOn, dhtService);

	}

	@SuppressWarnings("unused")
	private static void initializeConfig(String appName) throws IOException {
		logger.debug("Initializeing Configs");
		if (appName == null) {
			PropertiesFactory.setConfDir("anonBit");
		} else {
			PropertiesFactory.setConfDir(appName);
		}
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
		externalCom = new TimmeeyHttpSimpleServer(gson,
				torSocketFactory.getServerSocket(Integer.parseInt(appProps
						.getProperty("externalCommPort"))));

		externalCom.startServer();
		logger.trace("Server initialized");

	}

	private static void initializeRequestService() {
		logger.debug("Initializing Request Service");

		LinkedBlockingQueue<Runnable> requestQueue = new LinkedBlockingQueue<Runnable>();
		ObjectPool<String, Socket> pool = getTorSocketPool();

		addMaintenanceTask(new Callable() {

			@Override
			public Object call() {
				logger.debug("RequestQueue is currently {} long",
						requestQueue.size());
				logger.debug(
						"SocketPool currently holds {} in total, with {} Sockets lend out",
						pool.totalPoolSize(), pool.currentlyLendOutObjects());
				return null;

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
				for (Callable callable : maintenanceTaskList) {
					try {
						callable.call();
					} catch (Exception e) {
						logger.warn("Maintenance callable threw exception: {}",
								e.getMessage());
					}
				}

			}
		};

		maintenanceTimer.schedule(task, 10000L, 10000L);
		addMaintenanceTask(new Callable() {

			@Override
			public Object call() {
				logger.debug(String.format("Curent total thread count: %s",
						ManagementFactory.getThreadMXBean().getThreadCount()));
				return null;

			}
		});

	}

	public static void addMaintenanceTask(Callable c) {
		maintenanceTaskList.add(c);

	}

	public static void emergencyShutdown(String reason, Throwable e) {
		try {

			logger.error(
					"Killing the application because an error occured: {}",
					reason);
			System.out.println(reason);
			if (e != null) {
				logger.error("An {} was provided: {}", e.getClass().toString(),
						e.getMessage());
				System.out.println(String.format("An %s was provided: %s", e
						.getClass().toString(), e.getMessage()));
			}
			Thread.sleep(300);
		} catch (InterruptedException e1) {
			// Doen't matter here
		} finally {
			System.out.println("oh fuck....." + reason);
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

	public static void registerShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				System.out.println("got shutdown request");
				logger.info("Got shutdown request");
			}
		});
	}

	private static void registerHandler() {
		PeerGroupMemberIpUpdateRequest.addHandler(externalCom,
				new PeerGroupMemberIpUpdateRequestHandler(peerGroupManager));
		PeerGroupUpdateRequest.addHandler(externalCom,
				new PeerGroupUpdateRequestHandler(peerGroupManager));
		PeerGroupApplicationRequest.addHandler(externalCom,
				new PeerGroupApplicationRequestHandler(peerGroupManager));
	}
}
