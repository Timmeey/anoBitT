package anoBitT;

import java.util.concurrent.Future;

import com.google.gson.Gson;

import de.timmeey.anoBitT.http.communication.HTTPRequest;
import de.timmeey.anoBitT.network.portSocketForwarder.SocketFactory;

public class DHTGetRequest extends HTTPRequest<DHTReply> {
	
	protected DHTGetRequest(SocketFactory socketFactory, String path, Gson gson) {
		super(socketFactory, path, gson);
		// TODO Auto-generated constructor stub
	}

	String key;
	String value;

	@Override
	public Future<HTTPResponse> send(String hostname) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected String getTransferRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

}
