package de.timmeey.anoBitT.dht.impl;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.google.common.base.Preconditions.checkNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.DHTGetRequest;
import de.timmeey.anoBitT.DHTPutRequest;
import de.timmeey.anoBitT.DHTReply;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.libTimmeey.exceptions.unchecked.NotYetImplementedException;
import de.timmeey.libTimmeey.networking.communicationClient.HTTPRequestService;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;

public class DHTServiceFakeImpl implements DHTService {
	private static final Logger logger = LoggerFactory
			.getLogger(DHTServiceFakeImpl.class);
	private final HTTPRequestService requestService;
	private final int DHTPort;
	private final String host;

	public DHTServiceFakeImpl(PropertiesAccessor props,
			HTTPRequestService requestService) {
		checkNotNull(props);
		this.requestService = checkNotNull(requestService);
		DHTPort = checkNotNull(Integer.parseInt(props.getProperty("DHTPort")));
		// this.host = "localhost";

		this.host = props.getProperty("DHTHostname");

	}

	@Override
	public boolean put(String key, String value, boolean waitForConfirmation) {
		logger.debug(
				"Putting DHT key: {} with {} into {} and {} wait for confirmation",
				key, value, host, waitForConfirmation);
		boolean result = !waitForConfirmation;
		DHTPutRequest putRequest = new DHTPutRequest(host, key, value);

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
				logger.warn("request threw InterruptedException: {}",
						e.getMessage());

			} catch (ExecutionException e) {

				e.printStackTrace();
				logger.warn("request threw ExecutionException: {}",
						e.getMessage());
			} catch (TimeoutException e) {
				e.printStackTrace();
				logger.warn("request threw TimeoutException: {}",
						e.getMessage());
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
