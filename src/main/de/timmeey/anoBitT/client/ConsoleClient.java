package de.timmeey.anoBitT.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import asg.cliche.Command;
import asg.cliche.ShellFactory;
import de.timmeey.anoBitT.communication.external.ExternalCommunicationHandler;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.dht.impl.DHTServiceFakeImpl;
import de.timmeey.anoBitT.network.impl.AnonSocketFactoryImpl;
import de.timmeey.anoBitT.network.impl.SocketFactoryImpl;
import de.timmeey.anoBitT.tor.KeyPair;
import de.timmeey.anoBitT.tor.TorManager;
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

public class ConsoleClient {
	private static final String confDir = "/home/timmeey/.anoBitT/";
	private final static long TORSOCKETIDLETIMOUT = 1000 * 60 * 15; // 15min
	private final static long TORSOCKETPOOLCLEANUPTIME = 1000 * 60 * 5; // 5
																		// min

	private static final NetSerializer gson = new GsonSerializer();

	private final ExecutorService torRequestExecutorService = new ThreadPoolExecutor(
			5, 30, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(65));

	private DHTService dht;

	public ConsoleClient(SocketFactory socketFactory) throws IOException,
			InterruptedException {
		PropertiesFactory.setConfDir("anonBit");

		PropertiesAccessor props = PropertiesFactory
				.getPropertiesAccessor("dht");

		System.out.println(socketFactory);
		HTTPRequestService requestService = new HTTPRequestHandlerImpl(
				socketFactory, new GsonSerializer(), torRequestExecutorService,
				getTorSocketPool());

		dht = new DHTServiceFakeImpl(props, requestService);
	}

	@Command
	// One,
	public boolean putDHT(String key, String value, boolean wait) {
		return dht.put(key, value, wait);
	}

	@Command
	// two,
	public String getValue(String key) {
		String result = dht.get(key);
		System.out.println(result);
		return result;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		PropertiesFactory.setConfDir("anonbit");

		TorManager tor = new TorManager(
				PropertiesFactory.getPropertiesAccessor("tor"));

		tor.startTor();
		SocketFactory torSocketFactory = tor.getTorSocketFactory();

		ShellFactory.createConsoleShell("hello", "",
				new ConsoleClient(torSocketFactory)).commandLoop(); // and
																	// three.

	}

	private static ObjectPool<String, Socket> getTorSocketPool() {
		Verifier<Socket> verifier = new Verifier<Socket>() {

			@Override
			public boolean verify(Socket object) {
				System.out.println("verifieng");
				return !object.isClosed();
			}
		};
		return new SimpleObjectPool<String, Socket>(TORSOCKETIDLETIMOUT,
				TORSOCKETPOOLCLEANUPTIME, verifier);
	}

}
