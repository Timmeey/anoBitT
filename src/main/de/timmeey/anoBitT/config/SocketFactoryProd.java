package de.timmeey.anoBitT.config;

import com.google.inject.AbstractModule;

import de.timmeey.anoBitT.config.GuiceAnnotations.AnonSocketFactory;
import de.timmeey.anoBitT.config.GuiceAnnotations.NonAnonSocketFactory;
import de.timmeey.anoBitT.network.impl.AnonSocketFactoryImpl;
import de.timmeey.anoBitT.network.impl.SocketFactoryImpl;
import de.timmeey.libTimmeey.networking.SocketFactory;

/**
 * This config class provides only non-tor sockets. It should be only used while
 * developing, and be removed from pdouction
 * 
 * @author timmeey
 *
 */
public class SocketFactoryProd extends AbstractModule {

	@Override
	protected void configure() {

		bind(SocketFactory.class).annotatedWith(AnonSocketFactory.class)
				.to(SocketFactoryImpl.class).asEagerSingleton();
		;
		bind(SocketFactory.class).annotatedWith(NonAnonSocketFactory.class)
				.to(SocketFactoryImpl.class).asEagerSingleton();
		;

	}

}
