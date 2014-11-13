package de.timmeey.anoBitT.tor;

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

}
