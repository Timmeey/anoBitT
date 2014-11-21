package de.timmeey.anoBitT.communication.communicationServer;

public interface HTTPFilter {

	public boolean doFilter(String path, HttpContext ctx);

}
