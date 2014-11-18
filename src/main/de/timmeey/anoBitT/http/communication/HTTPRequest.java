package de.timmeey.anoBitT.http.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

import com.google.gson.Gson;
import com.google.inject.Inject;

import de.timmeey.anoBitT.config.GuiceAnnotations.HTTPRequestExecutor;
import de.timmeey.anoBitT.network.portSocketForwarder.SocketFactory;

public abstract class HTTPRequest<T extends HTTPResponse> {
	transient protected final SocketFactory socketFactory;
	transient protected final String path;
	transient protected final Gson gson;
	transient private final ExecutorService execPool;
	transient private final String hostname;

	protected HTTPRequest(final String path, final String hostname,
			SocketFactory socketFactory, Gson gson, ExecutorService execService) {
		this.path = path;
		this.hostname = hostname;
		this.execPool = execService;
		this.gson = gson;
		this.socketFactory = socketFactory;
	}

	public abstract Future<T> send();

	protected <T extends HTTPResponse> Future<T> realSend(Class<T> clazz) {
		Callable<T> call = new Callable<T>() {

			public T call() throws Exception {
				// Thread.sleep(4000);
				return gson.fromJson(
						doPost(null, null, getTransferRepresentation()), clazz);
			}
		};
		return execPool.submit(call);

	}


	private String doPost(String host, String path, String data)
			throws IOException {
		try (// Socket socket = this.socketFactory.createPrivateSocket(host,
				// 8080);
		Socket socket = new Socket(this.hostname, 6574);
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

	@Override
	public String toString() {
		return this.getTransferRepresentation();
	}
}
