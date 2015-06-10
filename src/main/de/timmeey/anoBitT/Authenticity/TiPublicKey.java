package de.timmeey.anoBitT.Authenticity;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is only a wrapper class for public keys, so they can be serialized and
 * send over the line
 * 
 * @author timmeey
 *
 */
public class TiPublicKey {
	private final static Logger logger = LoggerFactory
			.getLogger(TiPublicKey.class);
	private final BigInteger exp, mod;

	public TiPublicKey(PublicKey publicKey) {
		RSAPublicKeySpec pub = null;
		try {
			KeyFactory fact = KeyFactory.getInstance("RSA");

			pub = fact.getKeySpec(publicKey, RSAPublicKeySpec.class);

		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			System.out.println(String.format("Cannot happen:%s", e));
			logger.error(
					"Can not happen ;-), if this happens here is the message: {}",
					e);

		}
		this.mod = pub.getModulus();
		this.exp = pub.getPublicExponent();

	}

	public PublicKey getPublicKey() {
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(mod, exp);
		KeyFactory fact;
		try {
			fact = KeyFactory.getInstance("RSA");
			PublicKey pubKey = fact.generatePublic(keySpec);
			return pubKey;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;

	}

}
