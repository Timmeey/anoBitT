package de.timmeey.anoBitT.network;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;

import org.silvertunnel_ng.netlib.adapter.url.NetlibURLStreamHandlerFactory;

import timmeeyLib.exceptions.unchecked.NotYetInitializedException;

import com.google.inject.Singleton;

@Singleton
public class UrlFactory {
	private NetlibURLStreamHandlerFactory netlibStreamHandlerFactory;

	UrlFactory() {
		netlibStreamHandlerFactory = null;
	}

	public void setNetlibStreamHandlerFactory(
			NetlibURLStreamHandlerFactory netlibStreamHandlerFactory) {
		this.netlibStreamHandlerFactory = netlibStreamHandlerFactory;
	}

	public URL getNonPrivateURL(String urlStr) throws MalformedURLException {
		return new URL(urlStr);
	}

	public URL getPrivateURL(String urlStr) throws MalformedURLException {
		if (this.netlibStreamHandlerFactory == null) {
			throw new NotYetInitializedException();
		}
		URLStreamHandler handler = this.netlibStreamHandlerFactory
				.createURLStreamHandler(new URL(urlStr).getProtocol());
		URL context = null;
		URL url = new URL(context, urlStr, handler);
		return url;
	}
}
