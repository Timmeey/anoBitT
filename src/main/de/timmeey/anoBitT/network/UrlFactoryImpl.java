package de.timmeey.anoBitT.network;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;

import org.silvertunnel_ng.netlib.adapter.url.NetlibURLStreamHandlerFactory;

import timmeeyLib.exceptions.unchecked.NotYetInitializedException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UrlFactoryImpl implements UrlFactory {
	private NetlibURLStreamHandlerFactory netlibStreamHandlerFactory;

	@Inject
	UrlFactoryImpl() {
		netlibStreamHandlerFactory = null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.timmeey.anoBitT.network.UrlFactory1#setNetlibStreamHandlerFactory(
	 * org.silvertunnel_ng.netlib.adapter.url.NetlibURLStreamHandlerFactory)
	 */
	@Override
	public void setNetlibStreamHandlerFactory(
			NetlibURLStreamHandlerFactory netlibStreamHandlerFactory) {
		this.netlibStreamHandlerFactory = netlibStreamHandlerFactory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.timmeey.anoBitT.network.UrlFactory1#getNonPrivateURL(java.lang.String)
	 */
	@Override
	public URL getNonPrivateURL(String urlStr) throws MalformedURLException {
		return getNonPrivateURL(urlStr, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.timmeey.anoBitT.network.UrlFactory1#getNonPrivateURL(java.lang.String,
	 * boolean)
	 */
	@Override
	public URL getNonPrivateURL(String urlStr, boolean keepAlive)
			throws MalformedURLException {
		// if (keepAlive)
		// this.keppAliveEnsurer.addHost(urlStr);

		return new URL(urlStr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.timmeey.anoBitT.network.UrlFactory1#getPrivateURL(java.lang.String,
	 * boolean)
	 */
	@Override
	public URL getPrivateURL(String urlStr, boolean keepAlive)
			throws MalformedURLException {
		if (this.netlibStreamHandlerFactory == null) {
			throw new NotYetInitializedException();
		}
		if (!urlStr.contains("http")) {
			urlStr = "http://" + urlStr;
		}
		URLStreamHandler handler = this.netlibStreamHandlerFactory
				.createURLStreamHandler(new URL(urlStr).getProtocol());
		URL context = null;
		URL url = new URL(context, urlStr, handler);

		return url;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.timmeey.anoBitT.network.UrlFactory1#getPrivateURL(java.lang.String)
	 */
	@Override
	public URL getPrivateURL(String urlStr) throws MalformedURLException {
		return getPrivateURL(urlStr, true);
	}
}
