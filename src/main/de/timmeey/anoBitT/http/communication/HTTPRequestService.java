package de.timmeey.anoBitT.http.communication;

import java.util.concurrent.Future;

public interface HTTPRequestService {

	public abstract <T extends HTTPResponse> Future<T> send(
			HTTPRequest request, Class<T> clazz);

	public abstract <T extends HTTPRequest> T deserializeRequest(String string,
			Class<T> clazz);

	public abstract String serializeHTTPResponse(HTTPResponse req);

}