package anoBitT;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;

import de.timmeey.anoBitT.config.AnonBitTModule;
import de.timmeey.anoBitT.config.DefaultsConfigModule;
import de.timmeey.anoBitT.config.SocketFactoryDev_nonAnon;
import de.timmeey.anoBitT.dht.DHTService;
import de.timmeey.anoBitT.network.impl.SocketFactoryImpl;
import de.timmeey.anoBitT.tor.TorManager;
import de.timmeey.libTimmeey.properties.PropertiesAccessor;
import de.timmeey.libTimmeey.properties.PropertiesFactory;
import asg.cliche.Command;
import asg.cliche.ShellFactory;

public class ConsoleClient {

	private DHTService dht;

	public ConsoleClient() throws IOException, InterruptedException {
		System.setProperty("http.keepAlive", "true");
		PropertiesFactory.setConfDir("anonBit");
		Injector injector = Guice.createInjector(new AnonBitTModule(),
				new DHTFakeServiceServerModule(), new DefaultsConfigModule(),
				new SocketFactoryDev_nonAnon());
		dht = injector.getInstance(DHTService.class);
		// injector.getInstance(TorManager.class).startTor();
	}

	@Command
	// One,
	public boolean putDHT(String key, String value, boolean wait) {
		return dht.put(key, value, wait);
	}

	@Command
	// two,
	public String getValue(String key) {
		String result = dht.get(key);
		System.out.println(result);
		return result;
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		ShellFactory.createConsoleShell("hello", "", new ConsoleClient())
				.commandLoop(); // and three.
	}

}
