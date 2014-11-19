package de.timmeey.anoBitT.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.silvertunnel_ng.netlib.api.NetSocket;

public class NonPrivateSocket extends Socket {

	protected NonPrivateSocket(String remoteAddress, int remotePort)
			throws UnknownHostException, IOException {
		super(InetAddress.getByName(remoteAddress), remotePort);
	}

}
