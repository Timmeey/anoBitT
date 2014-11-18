package anoBitT;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/dht-service")
public class DHTServlet {

	@GET
	@Path("/dht-get/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getDHT(@PathParam("key") String key) {

		return DHTServer.getValue(key);
	}

	@GET
	@Path("/dht-post/{key}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public String postDHT(@PathParam("key") String key,
			@PathParam("value") String value) {
		DHTServer.putValue(key, value);
		return value;
	}
}