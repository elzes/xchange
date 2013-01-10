package nl.groep5.xchange;

import java.io.IOException;
import java.net.Socket;

public class Test {

	public static void main(String[] args) {
		new Test();
	}

	private Socket socket;

	public Test() {
		try {
			socket = new Socket("192.168.1.2", 7000);
			socket.getOutputStream().write("TEST".getBytes());
			socket.getOutputStream().flush();
			socket.close();
			System.out.println("done");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
