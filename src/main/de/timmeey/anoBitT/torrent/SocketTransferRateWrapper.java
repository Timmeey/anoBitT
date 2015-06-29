package de.timmeey.anoBitT.torrent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SocketTransferRateWrapper extends Socket {
	private OutputStreamWrapper outputStream = null;
	private InputStreamWrapper inputStream = null;
	private Socket socket;

	private static final Logger logger = LoggerFactory
			.getLogger(SocketTransferRateWrapper.class);

	public SocketTransferRateWrapper(Socket socket) {
		this.socket = socket;
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
						System.out.print("Written: " + getWrittenBytes());
						System.out.println("Read: " + getReadBytes());
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

		}).start();
		;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (inputStream == null) {
			this.inputStream = new InputStreamWrapper(socket.getInputStream());
		}
		return this.inputStream;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		if (outputStream == null) {
			this.outputStream = new OutputStreamWrapper(
					socket.getOutputStream());
		}
		return this.outputStream;
	}

	public long getWrittenBytes() {
		if (outputStream != null) {
			return this.outputStream.byteCount.get();
		} else {
			return 0;
		}
	}

	public long getReadBytes() {
		if (inputStream != null) {
			return this.inputStream.byteCount.get();
		} else {
			return 0;
		}
	}

}

class OutputStreamWrapper extends OutputStream {
	private final OutputStream out;
	public AtomicLong byteCount = new AtomicLong(0);

	public OutputStreamWrapper(OutputStream out) {
		this.out = out;

	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
		byteCount.incrementAndGet();

	}

}

class InputStreamWrapper extends InputStream {
	private final InputStream in;
	public AtomicLong byteCount = new AtomicLong(0);

	public InputStreamWrapper(InputStream in) {
		this.in = in;

	}

	@Override
	public int read() throws IOException {
		byteCount.incrementAndGet();
		return in.read();
	}

}
