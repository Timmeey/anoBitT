package de.timmeey.anoBitT.communication.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import timmeeyLib.pooling.ObjectPool;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.communication.HTTPRequest;
import de.timmeey.anoBitT.communication.HTTPRequestService;
import de.timmeey.anoBitT.communication.HTTPResponse;
import de.timmeey.anoBitT.communication.communicationServer.AnonBitMessage;
import de.timmeey.anoBitT.config.GuiceAnnotations.HTTPRequestExecutor;
import de.timmeey.anoBitT.config.GuiceAnnotations.HTTPRequestSocketPool;
import de.timmeey.anoBitT.network.impl.SocketFactoryImpl;

@Singleton
public class HTTPRequestHandlerImpl implements HTTPRequestService {
	protected final SocketFactoryImpl anonSocketFactory;
	protected final Gson gson;
	private final ExecutorService execPool;
	private final ObjectPool<String, Socket> socketPool;

	@Inject
	private HTTPRequestHandlerImpl(SocketFactoryImpl anonSocketFactory,
			Gson gson, @HTTPRequestExecutor ExecutorService execService,
			@HTTPRequestSocketPool ObjectPool<String, Socket> socketPool) {

		this.execPool = execService;
		this.gson = gson;
		this.anonSocketFactory = anonSocketFactory;
		this.socketPool = socketPool;

	}

	public <T extends HTTPResponse> Future<T> send(
			final HTTPRequest<?> request, final Class<T> clazz, final int port) {
		Callable<T> call = new Callable<T>() {

			public T call() throws Exception {
				// Thread.sleep(4000);
				T result = deserializeResponse(
						doPost(request.getHost(), request.getPath(),
								serializeHTTPRequest(request), port), clazz);
				if (result.getResponseCode() != 200) {
					System.out.println("Warning, response code was: "
							+ result.getResponseCode());
				}
				return result;
			}
		};
		return execPool.submit(call);
	}

	private String doPost(String host, String path, String data, int port)
			throws UnknownHostException, IOException {
		System.out.println("Posting " + data + "to " + host + path);
		Socket server;
		server = socketPool.borrow(host + ":" + port);
		if (server == null) {
			socketPool.store(host + ":" + port,
					anonSocketFactory.getSocket(host, port));
			server = socketPool.borrow(host + ":" + port);
		}

		BufferedWriter bufW = new BufferedWriter(new OutputStreamWriter(
				server.getOutputStream()));
		BufferedReader bufR = new BufferedReader(new InputStreamReader(
				server.getInputStream()));
		AnonBitMessage msg = new AnonBitMessage(path, data);

		bufW.write(gson.toJson(msg) + "\n");
		bufW.flush();
		String answer = bufR.readLine();
		socketPool.giveBack(server);
		return answer;

	}

	private String serializeHTTPRequest(HTTPRequest<?> req) {
		return gson.toJson(req);
	}

	private <T extends HTTPResponse> T deserializeResponse(String string,
			Class<T> clazz) {
		return gson.fromJson(string, clazz);
	}

}
