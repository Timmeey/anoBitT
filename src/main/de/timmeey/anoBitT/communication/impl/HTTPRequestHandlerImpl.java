package de.timmeey.anoBitT.communication.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.timmeey.anoBitT.communication.HTTPRequest;
import de.timmeey.anoBitT.communication.HTTPRequestService;
import de.timmeey.anoBitT.communication.HTTPResponse;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.timmeey.anoBitT.http.communication.HTTPRequestService#send(de.timmeey
	 * .anoBitT.http.communication.HTTPRequest, java.lang.Class)
	 */
	@Override
	public <T extends HTTPResponse> Future<T> send(HTTPRequest<?> request,
			Class<T> clazz) {
		Callable<T> call = new Callable<T>() {

			public T call() throws Exception {
				// Thread.sleep(4000);
				return gson.fromJson(
						doPost(request.getHost(), request.getPath(),
								serializeHTTPRequest(request)), clazz);
			}
		};
		return execPool.submit(call);

	}

	private String doPost(String host, String path, String data)
			throws IOException {
		try (// Socket socket = this.socketFactory.createPrivateSocket(host,
				// 8080);
		Socket socket = new Socket(host, httpRequestServerPort);
				BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
						socket.getOutputStream(), "UTF8"));
				BufferedReader rd = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));) {
			String encData = URLEncoder.encode("key1", "UTF-8") + "="
					+ URLEncoder.encode(data, "UTF-8");

			wr.write("POST " + path + " HTTP/1.0\r\n");
			wr.write("Content-Length: " + data.length() + "\r\n");
			wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
			wr.write("\r\n");

			wr.write(data);
			wr.flush();

			String part;
			String response = "";
			while ((part = rd.readLine()) != null) {
				response += part;
			}
			return response;
		}

	}

	private String serializeHTTPRequest(HTTPRequest req) {
		return gson.toJson(req);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.timmeey.anoBitT.http.communication.HTTPRequestService#deserializeRequest
	 * (java.lang.String, java.lang.Class)
	 */
	@Override
	public <T extends HTTPRequest<?>> T deserializeRequest(String string,
			Class<T> clazz) {
		return gson.fromJson(string, clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.timmeey.anoBitT.http.communication.HTTPRequestService#deserializeRequest
	 * (java.lang.String, java.lang.Class)
	 */
	@Override
	public <T extends HTTPRequest<?>> T deserializeRequest(Reader reader,
			Class<T> clazz) {
		return gson.fromJson(reader, clazz);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.timmeey.anoBitT.http.communication.HTTPRequestService#
	 * serializeHTTPResponse(de.timmeey.anoBitT.http.communication.HTTPRequest)
	 */
	@Override
	public String serializeHTTPResponse(HTTPResponse req) {
		return gson.toJson(req);
	}

	private <T extends HTTPResponse> T deserializeResponse(String string,
			Class<T> clazz) {
		return gson.fromJson(string, clazz);
	}
}
