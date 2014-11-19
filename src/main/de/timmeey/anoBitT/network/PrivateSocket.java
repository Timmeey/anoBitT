package de.timmeey.anoBitT.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.silvertunnel_ng.netlib.api.NetSocket;

public class PrivateSocket extends Socket {
	private NetSocket socket;

	protected PrivateSocket(NetSocket socket) {
		this.socket = socket;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return socket.getOutputStream();
	}

	@Override
	public synchronized void close() throws IOException {
		// TODO Auto-generated method stub
		socket.close();
	}

}
