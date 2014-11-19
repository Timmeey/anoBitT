package anoBitT;

import java.io.IOException;

import asg.cliche.Command;
import asg.cliche.ShellFactory;

public class ConsoleClient {

	@Command
	// One,
	public String hello() {
		return "Hello, World!";
	}

	@Command
	// two,
	public int add(int a, int b) {
		return a + b;
	}

	public static void main(String[] args) throws IOException {
		ShellFactory.createConsoleShell("hello", "", new ConsoleClient())
				.commandLoop(); // and three.
	}

}
