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

import timmeeyLib.properties.PropertiesAccessor;
import timmeeyLib.properties.PropertiesFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.main;
import de.timmeey.anoBitT.config.GuiceAnnotations.TorProperties;
import de.timmeey.anoBitT.exceptions.NotAnonymException;
import de.timmeey.anoBitT.network.SocketFactory;
import de.timmeey.anoBitT.network.UrlFactory;

@Singleton
public class TorManager {

	private KeyPair keyPair;
	private NetLayer torNetLayer;
	private final SocketFactory sockFac;
	private final PropertiesAccessor torProps;
	private final UrlFactory urlFactory;

	@Inject
	TorManager(@TorProperties PropertiesAccessor torProps,
			SocketFactory sockFac, UrlFactory urlFactory) {
		keyPair = null;
		this.sockFac = sockFac;
		this.torProps = torProps;
		this.urlFactory = urlFactory;

	}

	public TorManager startTor() throws IOException, InterruptedException {
		NameServiceGlobalUtil.initNameService();

		torNetLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
		// new Thread(new Runnable() {
		// public void run() {
		// double lastStatus = -1;
		// while (true) {
		// if (torNetLayer.getStatus().getReadyIndicator() != lastStatus) {
		// lastStatus = torNetLayer.getStatus()
		// .getReadyIndicator();
		// System.out.println(torNetLayer.getStatus());
		// }
		// if (torNetLayer.getStatus().getReadyIndicator() == 1) {
		// break;
		// }
		// try {
		// Thread.sleep(5);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		// }
		// }).start();

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

		TorHiddenServicePrivateNetAddress hiddenAddress = torNetLayerUtil
				.parseTorHiddenServicePrivateNetAddressFromStrings(
						torProps.getProperty("torHiddenServicePrivateKey"),
						torProps.getProperty("hiddenServiceHostName"), true);

		System.out.println("Waiting for tor to complete");
		torNetLayer.waitUntilReady();

		// get the name service the corresponds to the netLayer
		NetAddressNameService ns = torNetLayer.getNetAddressNameService();

		// redirect to the selected name service implementation
		NameServiceGlobalUtil.setIpNetAddressNameService(ns);

		sockFac.setHiddenAddress(hiddenAddress);
		sockFac.setNetLayer(torNetLayer);

		this.keyPair = new KeyPair(hiddenAddress.getPublicKey().toString(),
				hiddenAddress.getPrivateKey().toString(),
				hiddenAddress.getPublicOnionHostname());

		NetlibURLStreamHandlerFactory tmp = new NetlibURLStreamHandlerFactory(
				true);
		this.urlFactory.setNetlibStreamHandlerFactory(tmp);
		tmp.setNetLayerForHttpHttpsFtp(torNetLayer);
		if (!anonymSelfTest()) {
			return this;
		} else {
			// Something went terribly wrong here. We are not communicating
			// anonymous
			sockFac.setHiddenAddress(null);
			sockFac.setNetLayer(null);
			this.keyPair = null;
			throw new NotAnonymException();
		}
	}

	public KeyPair getKeyPair() {
		return this.keyPair;
	}

	private boolean anonymSelfTest() throws IOException {
		URL whatismyip = this.urlFactory
				.getNonPrivateURL("http://checkip.amazonaws.com/");
		BufferedReader in = new BufferedReader(new InputStreamReader(
				whatismyip.openStream()));

		String ip = in.readLine(); // you get the IP as a String
		System.out.println("nonPrivate ip: " + ip);

		URL whatIsMyPrivateIp = this.urlFactory
				.getPrivateURL("http://checkip.amazonaws.com/");
		BufferedReader privateIn = new BufferedReader(new InputStreamReader(
				whatIsMyPrivateIp.openStream()));

		String privateIp = privateIn.readLine(); // you get the IP as a String
		System.out.println("Private ip: " + privateIp);
		return false;
		// return !(ip.equalsIgnoreCase(privateIp));
	}

}
