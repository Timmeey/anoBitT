package de.timmeey.anoBitT.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

import de.timmeey.libTimmeey.networking.NetSerializer;

public class GsonSerializer implements NetSerializer {
	Gson gson = new GsonBuilder().registerTypeAdapter(PublicKey.class,
			new PublicKeyInstanceCreator()).create();

	@Override
	public String toJson(Object object) {
		return gson.toJson(object);
	}

	@Override
	public <T> T fromJson(String string, Class<T> clazz) {
		return gson.fromJson(string, clazz);
	}

}

class AnnotationExclusionStrategy implements ExclusionStrategy {

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return f.getAnnotation(Exclude.class) != null;
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return false;
	}
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Exclude {
}

class PublicKeyInstanceCreator implements
		InstanceCreator<java.security.PublicKey> {
	public PublicKey createInstance(Type type) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");

			kpg.initialize((int) Math.pow(2, 10));
			KeyPair kp = kpg.genKeyPair();
			return kp.getPublic();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			System.out
					.println("NoSuchAlgorithm can't happen because RSA is hardcoded");
		}
		return null;
	}
}
