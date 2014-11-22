package anoBitT;

import de.timmeey.libTimmeey.networking.HTTPRequest;
import de.timmeey.libTimmeey.networking.communicationServer.HttpHandler;
import de.timmeey.libTimmeey.networking.communicationServer.TimmeeyHttpSimpleServer;

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
