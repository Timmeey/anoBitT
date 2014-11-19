package de.timmeey.anoBitT.communication;

import java.io.Reader;
import java.util.concurrent.Future;

public interface HTTPRequestService {

	public <T extends HTTPResponse> Future<T> send(HTTPRequest<?> request,
			Class<T> clazz);

	public <T extends HTTPRequest<?>> T deserializeRequest(String string,
			Class<T> clazz);

	public <T extends HTTPRequest<?>> T deserializeRequest(Reader reader,
			Class<T> clazz);

	public String serializeHTTPResponse(HTTPResponse req);

}