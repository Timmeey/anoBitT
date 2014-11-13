package de.timmeey.anoBitT.peerGroup.Member;

import java.util.concurrent.Future;

import de.timmeey.anoBitT.Authenticity.PublicKey;

public abstract class Member {

	public abstract PublicKey getPublicKey();

	public abstract String getIpAddress();

	public abstract String getOnionAddress();

	public abstract Member updateIpAddress(String ipAddress);

	public abstract Future<Member> updateIpAddress();

}
