package de.timmeey.anoBitT.communication.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.communication.HTTPRequest;
import de.timmeey.anoBitT.communication.HTTPRequestService;
import de.timmeey.anoBitT.communication.HTTPResponse;
import de.timmeey.anoBitT.communication.httpServer.AnonBitMessage;
import de.timmeey.anoBitT.config.GuiceAnnotations.HTTPRequestExecutor;
import de.timmeey.anoBitT.config.GuiceAnnotations.HttpExternalServerPort;
import de.timmeey.anoBitT.network.SocketFactory;

@Singleton
public class HTTPRequestHandlerImpl implements HTTPRequestService {
	protected final SocketFactory socketFactory;
	protected final Gson gson;
	private final ExecutorService execPool;
	private final int httpRequestServerPort;

	@Inject
	private HTTPRequestHandlerImpl(SocketFactory socketFactory, Gson gson,
			@HTTPRequestExecutor ExecutorService execService,
			@HttpExternalServerPort int httpRequestServerPort) {

		this.execPool = execService;
		this.gson = gson;
		this.socketFactory = socketFactory;
		this.httpRequestServerPort = httpRequestServerPort;

	}

	@Override
	public <T extends HTTPResponse> Future<T> send(
			final HTTPRequest<?> request, Class<T> clazz) {
		return send(request, clazz, httpRequestServerPort);

	}

	public <T extends HTTPResponse> Future<T> send(
			final HTTPRequest<?> request, Class<T> clazz,
			boolean keepConnectionAlive) {
		return send(request, clazz, httpRequestServerPort);

	}

	public <T extends HTTPResponse> Future<T> send(
			final HTTPRequest<?> request, final Class<T> clazz, final int port) {
		Callable<T> call = new Callable<T>() {

			public T call() throws Exception {
				// Thread.sleep(4000);
				return deserializeResponse(
						doPost(request.getHost(), request.getPath(),
								serializeHTTPRequest(request), port), clazz);
			}
		};
		return execPool.submit(call);
	}

	private String doPost(String host, String path, String data, int port)
			throws UnknownHostException, IOException {
		System.out.println("Posting " + data + "to " + host + path);
		Socket server = socketFactory.getPrivateSocket(host, port);
		BufferedWriter bufW = new BufferedWriter(new OutputStreamWriter(
				server.getOutputStream()));
		BufferedReader bufR = new BufferedReader(new InputStreamReader(
				server.getInputStream()));
		AnonBitMessage msg = new AnonBitMessage(path, data);

		bufW.write(gson.toJson(msg) + "\n");
		bufW.flush();
		System.out.println("Flushed");
		String answer = bufR.readLine();
		System.out.println("Answer was");

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
