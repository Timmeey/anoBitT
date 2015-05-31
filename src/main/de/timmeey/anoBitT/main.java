package de.timmeey.anoBitT;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

import de.timmeey.anoBitT.communication.external.ExternalCommunicationHandler;
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

public class main {
	private static final String confDir = "/home/timmeey/.anoBitT/";
	private final static long TORSOCKETIDLETIMOUT = 1000 * 60 * 15; // 15min
	private final static long TORSOCKETPOOLCLEANUPTIME = 1000 * 60 * 5; // 5 min

	private static HTTPRequestService requestService;
	private static KeyPair keyPair;

	private static SocketFactory socketFactory;
	private static SocketFactory torSocketFactory;
	private static TorManager torManager;
	private static PropertiesAccessor appProps;
	private static final NetSerializer gson = new GsonSerializer();

	private static final ExecutorService torRequestExecutorService = new ThreadPoolExecutor(
			5, 30, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(65));

	public static void main(String[] args) throws Exception {
		firstConf();

		TorManager tor = new TorManager(
				PropertiesFactory.getPropertiesAccessor("tor"));
		socketFactory = new SocketFactoryImpl();
		torSocketFactory = tor.getTorSocketFactory();
		ServerSocket serverSocket = torSocketFactory.getServerSocket(Integer
				.parseInt(appProps.getProperty("commPort", "8888")));

		requestService = new HTTPRequestHandlerImpl(torSocketFactory, gson,
				torRequestExecutorService, getTorSocketPool());

		ExternalCommunicationHandler externalCom = new ExternalCommunicationHandler(
				new GsonSerializer(), appProps, torSocketFactory);

		// tor.startTor();
		keyPair = tor.getKeyPair();
		// requestService;

		externalCom.startServer(8888);

	}

	private static void firstConf() throws IOException {
		PropertiesFactory.setConfDir("anonBit");

		appProps = PropertiesFactory.getPropertiesAccessor("app");
	}

	private static ObjectPool<String, Socket> getTorSocketPool() {
		Verifier<Socket> verifier = new Verifier<Socket>() {

			@Override
			public boolean verify(Socket object) {
				return !object.isClosed();
			}
		};
		return new SimpleObjectPool<String, Socket>(TORSOCKETIDLETIMOUT,
				TORSOCKETPOOLCLEANUPTIME, verifier);
	}
}
