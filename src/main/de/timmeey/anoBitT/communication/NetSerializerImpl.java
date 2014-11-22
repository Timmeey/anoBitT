package de.timmeey.anoBitT.communication;

import timmeeyLib.networking.NetSerializer;

import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class NetSerializerImpl implements NetSerializer {
	private final Gson gson;

	@Inject
	protected NetSerializerImpl(Gson gson) {
		this.gson = gson;
	}

	@Override
	public String toJson(Object object) {
		return gson.toJson(object);
	}

	@Override
	public <T> T fromJson(String string, Class<T> clazz) {
		return gson.fromJson(string, clazz);
	}

}
