package de.timmeey.anoBitT.http.external;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import de.timmeey.anoBitT.http.external.views.EntryPoint;

public class ExternalServer {

	private static Server server;

	private static Server createJettyServer() {
		ServletContextHandler context = new ServletContextHandler(
				ServletContextHandler.SESSIONS);
		context.setContextPath("/");

		Server jettyServer = new Server(8080);
		jettyServer.setHandler(context);

		ServletHolder jerseyServlet = context.addServlet(
				org.glassfish.jersey.servlet.ServletContainer.class, "/*");
		jerseyServlet.setInitOrder(0);

		// Tells the Jersey Servlet which REST service/class to load.
		jerseyServlet.setInitParameter(
				"jersey.config.server.provider.classnames",
				EntryPoint.class.getCanonicalName());
		return jettyServer;

	}

	public static void start() throws Exception {
		if (server == null) {
			server = createJettyServer();
			try {
				server.start();
			} catch (Exception e) {
				server.destroy();
				throw e;
			}
		} else {

		}
	}

	public static void stop() throws InterruptedException {
		if (server != null) {
			try {
				server.join();
			} finally {
				server.destroy();
				server = null;
			}
		}
	}

}
