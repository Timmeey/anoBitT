package de.timmeey.anoBitT.http.communication.impls;

import com.google.gson.Gson;
import com.google.inject.Inject;

import de.timmeey.anoBitT.http.communication.HTTPResponse;

public class getAccountResponse extends HTTPResponse {
	public long accountId = 5;

	@Inject
	public getAccountResponse(Gson gson) {
		super(gson);
	}

}
