package de.timmeey.anoBitT.network;

import java.net.MalformedURLException;
import java.net.URL;

import org.silvertunnel_ng.netlib.adapter.url.NetlibURLStreamHandlerFactory;

public interface UrlFactory {

	public abstract void setNetlibStreamHandlerFactory(
			NetlibURLStreamHandlerFactory netlibStreamHandlerFactory);

	public abstract URL getNonPrivateURL(String urlStr)
			throws MalformedURLException;

	public abstract URL getNonPrivateURL(String urlStr, boolean keepAlive)
			throws MalformedURLException;

	public abstract URL getPrivateURL(String urlStr, boolean keepAlive)
			throws MalformedURLException;

	public abstract URL getPrivateURL(String urlStr)
			throws MalformedURLException;

}