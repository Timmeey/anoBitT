package de.timmeey.anoBitT.util;

import com.google.gson.Gson;

import de.timmeey.libTimmeey.networking.NetSerializer;

public class GsonSerializer implements NetSerializer {
	Gson gson = new Gson();

	@Override
	public String toJson(Object object) {
		return gson.toJson(object);
	}

	@Override
	public <T> T fromJson(String string, Class<T> clazz) {
		return gson.fromJson(string, clazz);
	}

}
