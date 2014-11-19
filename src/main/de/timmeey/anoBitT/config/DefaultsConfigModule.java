package de.timmeey.anoBitT.config;

import java.io.IOException;

import timmeeyLib.exceptions.unchecked.NotYetInitializedException;
import timmeeyLib.properties.PropertiesFactory;

import com.google.inject.AbstractModule;

import de.timmeey.anoBitT.config.GuiceAnnotations.DHTExternalPort;
import de.timmeey.anoBitT.config.GuiceAnnotations.HttpExternalServerPort;

public class DefaultsConfigModule extends AbstractModule {

	@Override
	protected void configure() {
		try {
			bind(Integer.class).annotatedWith(HttpExternalServerPort.class)
					.toInstance(
							Integer.parseInt(PropertiesFactory
									.getPropertiesAccessor("app").getProperty(
											"externalHttpCommunicationPort",
											"62752")));

			bind(Integer.class).annotatedWith(DHTExternalPort.class)
					.toInstance(
							Integer.parseInt(PropertiesFactory
									.getPropertiesAccessor("dht").getProperty(
											"dhtExternalPort", "62352")));

		} catch (NumberFormatException | NotYetInitializedException
				| IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
