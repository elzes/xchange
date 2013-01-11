import java.io.*;
import java.net.*;

/*
 * Character Streams that Use Byte Streams : InputStreamReader and OutputStreamWriter
 * Here line-oriented I/O is used : BufferedReader and PrintWriter
 *
 */

public class Tclient
{
	static final int PORT = 7000;

	/*
	 * main method
	 * 
	 */
       
	public static void main(String[] args) throws IOException {
		
		Socket s = null;
		PrintWriter out = null;
		BufferedReader in = null;

		try {
			// local loopback
			//s = new Socket("127.0.0.1", PORT);
			s = new Socket("192.168.1.2", PORT);
			// get socket input stream and open a BufferedReader on it
			in = new BufferedReader(
					new InputStreamReader(s.getInputStream()));
			
			// get socket output stream and open a PrintWriter on it
			out = new PrintWriter(s.getOutputStream(), true);
		} catch (UnknownHostException e) {
			System.err.println("Host unknown");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("I/O error");
			System.exit(1);
		}

		BufferedReader stdIn = new BufferedReader(
						  new InputStreamReader(System.in));
		String userInput; // a line with '\n'
		
		System.out.println("Max clients = 3");
		System.out.println("Type your input; \"STOP<return>\" will stop the server and ^D will stop the client");
		// finish with ctrl-d
		while ((userInput = stdIn.readLine()) != null) {
			out.println(userInput);
			System.out.println("Client received :" + in.readLine());
		}

		out.close();
		in.close();
		stdIn.close();
		s.close();
	}
}
