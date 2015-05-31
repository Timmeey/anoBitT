package de.timmeey.anoBitT.tor;

import java.util.HashMap;
import java.util.Map;

import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMember;

/**
 * The KeyPair class is intended to be used as a secure container to handle
 * private/public keys. It does not allow acces to the plain private key, but
 * provides methods to sign and decrypt messages using the private key. This is
 * the only class which should know about the private key (besides the
 * TorManager class) CAUTION: This is not a real security measure. The private
 * key can still be read from the config file or accessed via reflection. this
 * class is only here to provide some security against accidental key-leaks.
 * 
 * @author timmeey
 *
 */
public class KeyPair {
	private final String privateKey;
	private final String publicKey;
	private final String onionAddress;

	protected KeyPair(String publicKey, String privateKey, String onionAddress) {
		this.publicKey = publicKey;
		this.privateKey = privateKey;
		this.onionAddress = onionAddress;
	}

	public String getPublicKey() {
		return this.publicKey;
	}

	public String getOnionAddress() {
		return this.onionAddress;
	}

	public String signMsg(String msg) {
		return msg;
		// TODO
	}

	public String decryptMsg(String msg) {
		return msg;
		// TODO
	}

	public String encryptMsg(String msg) {
		return msg;
		// TODO
	}

	public Map<String, String> getAuthMapForMessage(PeerGroupMember recipient) {
		Map<String, String> authMap = new HashMap<String, String>();
		authMap.put("signedRecepient",
				this.signMsg(recipient.getOnionAddress()));
		authMap.put("sender", this.getOnionAddress());

		return authMap;
	}

}
