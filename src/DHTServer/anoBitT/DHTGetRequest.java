package anoBitT;

import timmeeyLib.networking.HTTPRequest;
import timmeeyLib.networking.communicationServer.HttpHandler;
import timmeeyLib.networking.communicationServer.TimmeeyHttpSimpleServer;

public class DHTGetRequest extends HTTPRequest<DHTReply> {
	transient public final static String path = "/dht-service/get";

	private final String key;

	public DHTGetRequest(String host, String key) {
		super(host, path, DHTReply.class);
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public static void addHandler(TimmeeyHttpSimpleServer server,
			HttpHandler handler) {
		server.registerHandler(path, handler);
	}

}
