package de.timmeey.anoBitT.communication.communicationServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.inject.Inject;

public class TimmeeyHttpSimpleServer extends Thread {
	private ServerSocket serverSocket;
	private final Map<String, HttpHandler> handlerList = new HashMap<String, HttpHandler>();
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
							handler.handle(ctx);
							System.out.println("Was handled");
							bufWrite.write(ctx.getResponse() + "\n");
							bufWrite.flush();
							System.out.println("Flushed");
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
}
