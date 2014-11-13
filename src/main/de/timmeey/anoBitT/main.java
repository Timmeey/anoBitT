package de.timmeey.anoBitT;

import java.io.IOException;

import org.silvertunnel_ng.netlib.api.NetLayer;

import timmeeyLib.properties.PropertiesFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;

import de.timmeey.anoBitT.config.AnonBitTModule;
import de.timmeey.anoBitT.http.external.ExternalServer;
import de.timmeey.anoBitT.tor.TorManager;

public class main {
	public static NetLayer torNetLayer;
	public static final String confDir = "/home/timmeey/.anoBitT/";

	public static void main(String[] args) throws Exception {
		firstConf();

		Injector injector = Guice.createInjector(new AnonBitTModule());

		/*
		 * Now that we've got the injector, we can build objects.
		 */
		TorManager tor = injector.getInstance(TorManager.class);

		tor.startTor();

		ExternalServer.start();
		// SocketFactory.createTorServerSocketToLocalServerSocketForward(80,
		// 8080);

	}

	private static void firstConf() throws IOException {
		PropertiesFactory.setConfDir("anonBit");
	}
}
