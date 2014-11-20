package de.timmeey.anoBitT.communication;

import java.util.concurrent.Future;

public interface HTTPRequestService {

	public <T extends HTTPResponse> Future<T> send(HTTPRequest<?> request,
			Class<T> clazz);

	public <T extends HTTPResponse> Future<T> send(HTTPRequest<?> request,
			Class<T> clazz, int port);

}