package anoBitT;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import de.timmeey.anoBitT.communication.HTTPRequest;

public class DHTPutRequest extends HTTPRequest<DHTReply> {
	transient private final static String path = "/dht-service/put";
	final String key;
	final String value;

	public DHTPutRequest(String hostname, String key, String value) {
		super(hostname, path, DHTReply.class);
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	/**
	 * THis method is meant to setup the contexHandlers for the httpRequests.
	 * Its just nice to not have to enter the path at multiple locations. so the
	 * httprequest OBJECT also sets the handler and the path accordingly. Again
	 * just a nice feature, nothing that is enforced
	 * 
	 * @param server
	 */
	public static void addHandler(HttpServer server, HttpHandler handler) {
		server.createContext(path, handler);
	}
}
