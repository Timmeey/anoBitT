package de.timmeey.anoBitT.Authenticity;

public abstract class PublicKey {

	public abstract String getPublicKey();

	public abstract String getOnionAddress();

	public abstract boolean verifyOnionAddress(String onionAddress);

}
