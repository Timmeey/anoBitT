package de.timmeey.anoBitT;

import java.io.IOException;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import de.timmeey.anoBitT.communication.external.ExternalCommunicationHandler;
import de.timmeey.anoBitT.config.AnonBitTModule;
import de.timmeey.anoBitT.config.DefaultsConfigModule;
import de.timmeey.anoBitT.config.SocketFactoryDev_nonAnon;
import de.timmeey.anoBitT.tor.TorManager;
import de.timmeey.libTimmeey.properties.PropertiesFactory;

public class main {
	public static final String confDir = "/home/timmeey/.anoBitT/";

	public static void main(String[] args) throws Exception {
		firstConf();
		AbstractModule socketFactoryConf;
		socketFactoryConf = new SocketFactoryDev_nonAnon();
		// socketFactoryConf = new SocketFactoryProd();
		Injector injector = Guice.createInjector(new AnonBitTModule(),
				socketFactoryConf, new DefaultsConfigModule());

		/*
		 * Now that we've got the injector, we can build objects.
		 */
		TorManager tor = injector.getInstance(TorManager.class);
		ExternalCommunicationHandler externalCom = injector
				.getInstance(ExternalCommunicationHandler.class);

		tor.startTor();
		externalCom.startServer(8888);

	}

	private static void firstConf() throws IOException {
		PropertiesFactory.setConfDir("anonBit");
	}
}
