package de.timmeey.anoBitT.communication.communicationServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.inject.Inject;

import de.timmeey.anoBitT.communication.HTTPRequest;
import de.timmeey.anoBitT.communication.HTTPResponse;

public class TimmeeyHttpSimpleServer extends Thread {
	private ServerSocket serverSocket;
	private final Map<String, HttpHandler> handlerList = new HashMap<String, HttpHandler>();
	private final LinkedList<HTTPFilter> filter = new LinkedList<HTTPFilter>();
	private final Gson gson;

	@Inject
	public TimmeeyHttpSimpleServer(Gson gson) {
		this.gson = gson;

	}

	public void setServerSocket(ServerSocket server) {
		this.serverSocket = server;
		System.out.println("Starting thread");
		this.start();
		;
		System.out.println("Started");
	}

	/**
	 * New Filter are appended at the end of the list. THe Filter chain goes
	 * down from first added -> last added If only one filter returnes false the
	 * request is aborted and send back with a error message
	 * 
	 * @param filter
	 *            the filter to user
	 * @return whether the filter lets the request pass
	 */
	public TimmeeyHttpSimpleServer registerFilter(HTTPFilter filter) {
		this.filter.addLast(filter);
		return this;
	}

	public TimmeeyHttpSimpleServer removeFilter(HTTPFilter filter) {
		this.filter.remove(filter);
		return this;
	}

	public TimmeeyHttpSimpleServer registerHandler(String path,
			HttpHandler handler) {
		System.out.println("Adding handler: " + path);
		this.handlerList.put(path, handler);
		return this;
	}

	public void run() {
		while (true) {
			try {
				System.out.println("Waiting for clients");
				Socket client = this.serverSocket.accept();
				System.out.println("Woho, got a client");
				handleClient(client);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void handleClient(final Socket client) {
		new Thread() {

			@Override
			public void run() {
				try (BufferedReader bufRead = new BufferedReader(
						new InputStreamReader(client.getInputStream()));
						BufferedWriter bufWrite = new BufferedWriter(
								new OutputStreamWriter(client.getOutputStream()));) {

					client.setSoTimeout(1000 * 60 * 10);

					String line;
					System.out.println("Waiting for input");
					while ((line = bufRead.readLine()) != null) {
						System.out.println("Input was: " + line);
						AnonBitMessage message = gson.fromJson(line,
								AnonBitMessage.class);
						HttpContext ctx = new HttpContext(gson,
								message.getPayloadObject());
						HttpHandler handler = handlerList
								.get(message.getPath());
						if (handler != null) {
							// THERE HAPPENS THE MAGIC
							// Only calls the actual handler if every filter
							// said OK
							if (filterRequestShallPass(message.getPath(), ctx)) {
								handler.handle(ctx);
							} else {
								if (ctx.getResponseCode() == 0) {
									ctx.setResponseCode(500);
								}
							}
							bufWrite.write(ctx.getResponse() + "\n");
							bufWrite.flush();
						} else {
							System.out.println("No handler found");
							client.close();
						}

					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					try {
						client.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}.start();
	}

	public void unregister(String path) {
		handlerList.remove(path);

	}

	private boolean filterRequestShallPass(String path, HttpContext ctx) {
		try {
			for (HTTPFilter httpFilter : filter) {
				if (!httpFilter.doFilter(path, ctx)) {
					System.out.println("Filter: " + httpFilter
							+ ", prevented the request from passing down");
					return false;
				}
			}
			return true;
		} catch (Exception e) {
			System.out
					.println("Exceptioon while filtering. abort request handling");
			e.printStackTrace();
		}
		return false;
	}
}
