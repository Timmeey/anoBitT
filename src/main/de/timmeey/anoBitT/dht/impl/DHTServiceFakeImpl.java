package de.timmeey.anoBitT.dht.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import timmeeyLib.exceptions.unchecked.NotYetImplementedException;
import timmeeyLib.networking.SocketFactory;
import timmeeyLib.networking.communicationClient.HTTPRequestService;
import timmeeyLib.properties.PropertiesAccessor;
import anoBitT.DHTGetRequest;
import anoBitT.DHTPutRequest;
import anoBitT.DHTReply;

import com.google.inject.Inject;

import de.timmeey.anoBitT.config.GuiceAnnotations.DHTProperties;
import de.timmeey.anoBitT.config.GuiceAnnotations.ExternalHTTPRequestService;
import de.timmeey.anoBitT.config.GuiceAnnotations.NonAnonSocketFactory;
import de.timmeey.anoBitT.dht.DHTService;

public class DHTServiceFakeImpl implements DHTService {

	private final SocketFactory socketFactory;
	private final PropertiesAccessor props;
	private final HTTPRequestService requestService;
	private final int DHTPort;
	private final String host;

	@Inject
	protected DHTServiceFakeImpl(
			@NonAnonSocketFactory SocketFactory socketFactory,
			@DHTProperties PropertiesAccessor props,
			@ExternalHTTPRequestService HTTPRequestService requestService) {
		this.props = props;
		this.socketFactory = socketFactory;
		this.requestService = requestService;
		DHTPort = Integer.parseInt(props.getProperty("DHTPort", "62352"));
		// this.host = props.getProperty("DHTHostname");
		this.host = "localhost";

	}

	@Override
	public boolean put(String key, String value, boolean waitForConfirmation) {
		boolean result = !waitForConfirmation;
		DHTPutRequest putRequest = new DHTPutRequest(host, key, value);

		// DHTPutRequest putRequest = new DHTPutRequest("localhost", key,
		// value);
		Future<DHTReply> putReply = requestService.send(putRequest,
				putRequest.getResponseType(), DHTPort);
		if (waitForConfirmation) {
			DHTReply realReply;
			try {
				realReply = putReply.get(60, TimeUnit.SECONDS);
				result = realReply.getKey().equals(key)
						&& realReply.getValue().equals(value);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // catch (TimeoutException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
			catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return result;
	}

	public String get(String key) {
		String result = null;
		DHTGetRequest getRequest = new DHTGetRequest(host, key);
		// DHTGetRequest getRequest = new DHTGetRequest("localhost", key);

		Future<DHTReply> getReply = requestService.send(getRequest,
				getRequest.getResponseType(), DHTPort);
		try {
			DHTReply reply = getReply.get(60, TimeUnit.SECONDS);
			result = reply.getValue();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}

	public List<String> getNodes() {
		throw new NotYetImplementedException(
				"In this implementation of DHTService not available");
	}
}
