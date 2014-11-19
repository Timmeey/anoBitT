package anoBitT;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.timmeey.anoBitT.communication.HTTPRequest;

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

	public static void addHandler(HttpServer server, HttpHandler handler) {
		server.createContext(path, handler);
	}

}
