package de.timmeey.anoBitT.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.timmeey.libTimmeey.networking.NetSerializer;

public class GsonSerializer implements NetSerializer {
	Gson gson = new GsonBuilder().create();

	@Override
	public String toJson(Object object) {
		return gson.toJson(object);
	}

	@Override
	public <T> T fromJson(String string, Class<T> clazz) {
		return gson.fromJson(string, clazz);
	}

}
//
// class AnnotationExclusionStrategy implements ExclusionStrategy {
//
// @Override
// public boolean shouldSkipField(FieldAttributes f) {
// return f.getAnnotation(Exclude.class) != null;
// }
//
// @Override
// public boolean shouldSkipClass(Class<?> clazz) {
// return false;
// }
// }
//
// @Retention(RetentionPolicy.RUNTIME)
// @Target(ElementType.FIELD)
// @interface Exclude {
// }
