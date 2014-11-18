package de.timmeey.anoBitT.http.communication.impls;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.google.gson.Gson;
import com.google.inject.Inject;

import de.timmeey.anoBitT.http.communication.HTTPRequest;
import de.timmeey.anoBitT.network.portSocketForwarder.SocketFactory;

public class getAccountRequest extends HTTPRequest<getAccountResponse> {
	transient static private final String path = "/account/get";
	Long accountId;

	
	protected getAccountRequest(final String hostname,
			SocketFactory socketFactory, Gson gson, ExecutorService execPool) {
		super(path, hostname, socketFactory, gson, execPool);
	}

	public getAccountRequest setAccountId(Long id) {
		this.accountId = id;
		return this;
	}

	public Long getAccountId() {
		return accountId;
	}

	@Override
	public Future<getAccountResponse> send() {
		return this.realSend(getAccountResponse.class);
	}

}
