package nl.groep5.xchange.storageServer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;

import junit.framework.TestCase;
import nl.groep5.xchange.Settings;

import org.junit.Test;

public class StorageServerTest extends TestCase {

	private static Settings settings;

	@Test
	public static void testStorageServer() throws IOException,
			ClassNotFoundException, InterruptedException {
		settings = Settings.getInstance();
		settings.load();

		String filename = "test.png";
		File file = new File("testFiles/" + filename);
		RandomAccessFile raf = new RandomAccessFile(file, "r");

		File file2 = new File("testFiles/" + filename + "2");
		if (!file2.exists()) {
			file2.createNewFile();
		}
		RandomAccessFile raf2 = new RandomAccessFile(file2, "rw");

		long readed = 0;
		while (readed < raf.length()) {
			byte[] buffer;
			if (raf.length() - readed < Settings.getBlockSize()) {
				buffer = new byte[(int) (raf.length() - readed)];
			} else {
				buffer = new byte[Settings.getBlockSize()];
			}

			raf.read(buffer);
			raf2.write(buffer);
			readed += buffer.length;
			Socket socket = new Socket(settings.getStorageServerIp(),
					Settings.getStorageServerPort());

			System.out.println("Readed " + buffer.length + "total " + readed
					+ " From " + raf.length());

			PrintWriter writer = new PrintWriter(new OutputStreamWriter(
					socket.getOutputStream()), true);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			long seekDistance = raf.getFilePointer() - buffer.length;
			writer.println("POST" + Settings.getSplitChar() + filename
					+ Settings.getSplitChar() + raf.length()
					+ Settings.getSplitChar() + seekDistance
					+ Settings.getSplitChar() + (buffer.length));

			BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
					socket.getOutputStream());
			bufferedOutputStream.write(buffer);
			bufferedOutputStream.flush();
			bufferedOutputStream.close();

			System.out.println("written " + buffer.length);
			assertEquals("OK", reader.readLine());
			socket.close();
		}
		raf.close();
		raf2.close();
	}
}