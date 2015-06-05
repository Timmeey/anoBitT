package de.timmeey.anoBitT.tor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.silvertunnel_ng.netlib.adapter.nameservice.NameServiceGlobalUtil;
import org.silvertunnel_ng.netlib.adapter.url.NetlibURLStreamHandlerFactory;
import org.silvertunnel_ng.netlib.api.NetAddressNameService;
import org.silvertunnel_ng.netlib.api.NetFactory;
import org.silvertunnel_ng.netlib.api.NetLayer;
import org.silvertunnel_ng.netlib.api.NetLayerIDs;
import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePrivateNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.TorNetLayerUtil;
import org.silvertunnel_ng.netlib.layer.tor.util.Encryption;
import org.silvertunnel_ng.netlib.layer.tor.util.RSAKeyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.exceptions.NotAnonymException;
import de.timmeey.anoBitT.network.impl.AnonSocketFactoryImpl;
import de.timmeey.libTimmeey.exceptions.unchecked.NotYetInitializedException;
import de.timmeey.libTimmeey.networking.SocketFactory;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;

import static com.google.common.base.Preconditions.checkNotNull;

public class TorManager {
	private static final Logger logger = LoggerFactory
			.getLogger(TorManager.class);

	private KeyPair keyPair;
	private NetLayer torNetLayer;
	private TorHiddenServicePrivateNetAddress hiddenAddress;
	private final PropertiesAccessor torProps;

	public TorManager(PropertiesAccessor torProps) {
		this.keyPair = null;
		this.torProps = checkNotNull(torProps);

	}

	public TorManager startTor() throws IOException, InterruptedException {
		NameServiceGlobalUtil.initNameService();

		torNetLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
		new Thread(new Runnable() {
			public void run() {
				while (torNetLayer.getStatus().getReadyIndicator() != 1) {
					logger.info("Tor connection Status: {}",
							torNetLayer.getStatus());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}).start();

		TorNetLayerUtil torNetLayerUtil = TorNetLayerUtil.getInstance();

		if (!torProps.contains("torHiddenServicePrivateKey")
				|| !torProps.contains("hiddenServiceHostName")) {
			TorHiddenServicePrivateNetAddress hiddenAddress1 = torNetLayerUtil
					.createNewTorHiddenServicePrivateNetAddress();

			torProps.addProperty("torHiddenServicePrivateKey", Encryption
					.getPEMStringFromRSAKeyPair(new RSAKeyPair(hiddenAddress1
							.getPublicKey(), hiddenAddress1.getPrivateKey())));

			torProps.addProperty("hiddenServiceHostName",
					hiddenAddress1.getPublicOnionHostname());
		}

		this.hiddenAddress = torNetLayerUtil
				.parseTorHiddenServicePrivateNetAddressFromStrings(
						torProps.getProperty("torHiddenServicePrivateKey"),
						torProps.getProperty("hiddenServiceHostName"), true);

		logger.info("Waiting for tor to complete");
		torNetLayer.waitUntilReady();
		logger.info("Tor ready");

		// get the name service the corresponds to the netLayer
		NetAddressNameService ns = torNetLayer.getNetAddressNameService();

		// redirect to the selected name service implementation
		NameServiceGlobalUtil.setIpNetAddressNameService(ns);

		this.keyPair = new KeyPair(hiddenAddress);

		NetlibURLStreamHandlerFactory tmp = new NetlibURLStreamHandlerFactory(
				true);
		tmp.setNetLayerForHttpHttpsFtp(torNetLayer);
		boolean testResult;
		try {
			testResult = isAnonymSelfTest();
		} catch (Exception e) {
			logger.error("Got Exception while testing anonymity. Aborting everything for security reasons!");
			// Something went terribly wrong here. We are not communicating
			// anonymous
			this.keyPair = null;
			throw new NotAnonymException();
		}

		if (testResult) {
			return this;
		} else {
			logger.error("Anonymity status test failed. Anonym IP seems to be the same like your IP. Aborting everything.");
			// Something went terribly wrong here. We are not communicating
			// anonymous
			this.keyPair = null;
			throw new NotAnonymException();
		}

	}

	public KeyPair getKeyPair() {
		return this.keyPair;
	}

	/**
	 * Calls an amazon ip two times, one time over a not anonym connection, and
	 * one time through the anonymitiy network. If the listed ips differ, the
	 * anonymity layer works
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean isAnonymSelfTest() throws IOException {
		logger.info("Starting anonymity Test");
		String urlStr = "http://checkip.amazonaws.com/";
		NetlibURLStreamHandlerFactory tmp = new NetlibURLStreamHandlerFactory(
				true);
		tmp.setNetLayerForHttpHttpsFtp(torNetLayer);

		URL context = null;
		URL whatIsMyIp = new URL(context, urlStr,
				tmp.createURLStreamHandler(new URL(urlStr).getProtocol()));

		BufferedReader in = new BufferedReader(new InputStreamReader(
				whatIsMyIp.openStream()));

		String ip = in.readLine(); // you get the IP as a String
		logger.info("Ip through TOR access: {}", ip);

		URL whatIsMyPrivateIp = new URL(urlStr);
		BufferedReader privateIn = new BufferedReader(new InputStreamReader(
				whatIsMyPrivateIp.openStream()));

		String privateIp = privateIn.readLine(); // you get the IP as a String
		logger.info("Real non-Tor IP: {}", privateIp);

		return (!ip.equalsIgnoreCase(privateIp));
	}

	public NetLayer getNetLayer() {
		return this.torNetLayer;
	}

	public SocketFactory getTorSocketFactory() {
		if (this.torNetLayer == null || this.hiddenAddress == null) {
			throw new NotYetInitializedException(
					"Tor service not yet initialized");
		}
		return new AnonSocketFactoryImpl(torNetLayer, hiddenAddress);
	}

}
