package de.timmeey.anoBitT.tor;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.silvertunnel_ng.netlib.layer.tor.TorHiddenServicePrivateNetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.timmeey.anoBitT.main;
import de.timmeey.anoBitT.peerGroup.Member.PeerGroupMember;
import de.timmeey.libTimmeey.crypto.Encryption;
import de.timmeey.libTimmeey.util.Base64Helper;

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
	private static final Logger logger = LoggerFactory.getLogger(KeyPair.class);
	private final TorHiddenServicePrivateNetAddress onionAddress;

	protected KeyPair(TorHiddenServicePrivateNetAddress onionAddress) {
		this.onionAddress = onionAddress;
	}

	public PublicKey getPublicKey() {
		return this.onionAddress.getPublicKey();
	}

	public String getOnionAddress() {
		return this.onionAddress.getPublicOnionHostname();
	}

	public byte[] signMsg(String msg) {
		try {
			return Encryption.sign(msg.getBytes("UTF-8"),
					this.onionAddress.getPrivateKey());
		} catch (InvalidKeyException | SignatureException
				| NoSuchAlgorithmException | UnsupportedEncodingException e) {
			main.emergencyShutdown(String.format(
					"An error occured while signing the message: %s", msg), e);
			return null;

		}
	}

	public boolean verifySignature(String message, byte[] digitalSignature,
			PeerGroupMember member) {
		logger.trace("verifySignature({},{},{})", message,
				digitalSignature.length, member.getOnionAddress());
		boolean result = false;
		try {
			result = Encryption.checkSignature(message.getBytes("UTF-8"),
					digitalSignature, member.getPublicKey());
			logger.trace("Call for checkSignature returned: {}", result);
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchProviderException | SignatureException
				| UnsupportedEncodingException e) {
			System.out.println(String.format(
					"OHOH das darf nciht sein.. %s, message %s", e.getClass(),
					e.getMessage()));
			e.printStackTrace();
			main.emergencyShutdown(
					String.format(
							"An error occured while verifying the message: %s, for member %s",
							member.getOnionAddress()), e);
			return false;

		}
		logger.trace("Verifying signature finished. reusult was: {}", result);
		return result;
	}

	public String decryptMsg(byte[] msg) {
		try {
			return new String(Encryption.decrypt(msg,
					this.onionAddress.getPrivateKey()), "UTF-8");
		} catch (InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | UnsupportedEncodingException e) {
			main.emergencyShutdown("An error occured while encrypting.", e);
			return null;
		}
	}

	public Map<String, String> getAuthMapForMessage(PeerGroupMember recipient) {
		Map<String, String> authMap = new HashMap<String, String>();
		authMap.put("signedRecepient", Base64Helper.byteToString(this
				.signMsg(recipient.getOnionAddress())));
		authMap.put("sender", this.getOnionAddress());

		return authMap;
	}

	public boolean verifyAuthMap(Map<String, String> authMap,
			PeerGroupMember sendingMember) {
		logger.debug("verifyAuthMap({},{})", authMap,
				sendingMember.getOnionAddress());
		String signedRecipient = authMap.get("signedRecepient");
		boolean result = verifySignature(getOnionAddress(),
				Base64Helper.stringToByte(signedRecipient), sendingMember);
		logger.debug("Verification of signature returend {} for {}", result,
				sendingMember.getOnionAddress());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format(
				"KeyPair [getPublicKey()=%s, getOnionAddress()=%s]",
				getPublicKey().getAlgorithm(), getOnionAddress());
	}
}
