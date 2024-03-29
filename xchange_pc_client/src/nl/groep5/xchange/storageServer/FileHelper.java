package nl.groep5.xchange.storageServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import nl.groep5.xchange.Settings;

public class FileHelper {

	public static final String TMP_EXT = ".!xch";
	public static final String INFO_EXT = ".!info";
	public static final int BLOCK_SIZE = Settings.getBlockSize();

	public static File createTmpFile(String location) throws IOException {
		File file = new File(location);
		if (!file.exists()) {
			file.createNewFile();
		}

		return file;
	}

	public static File loadFile(String location) {
		return new File(location);
	}

	public static RandomAccessFile getRandomAccesFileFromFileReadOnly(File file)
			throws FileNotFoundException {
		return new RandomAccessFile(file, "r");
	}

	public static RandomAccessFile getRandomAccesFileFromFile(File file)
			throws FileNotFoundException {
		return new RandomAccessFile(file, "rw");
	}

	/**
	 * 
	 * @param location
	 *            the complete location of the file
	 * @return
	 */
	public static File renameTmpFile(String location) {
		String newLocation = location.substring(0,
				location.length() - TMP_EXT.length());
		File oldfile = new File(location);
		File newfile = new File(newLocation);
		oldfile.renameTo(newfile);
		return newfile;
	}

	public static void createRandomAccesFileFromFile(File file, long size)
			throws IOException {
		RandomAccessFile raf = getRandomAccesFileFromFile(file);
		raf.setLength(size);
		raf.close();
	}

	public static void writeByteArrayToFile(File file, int offset,
			byte[] byteArray, int writeSize) throws IOException {
		RandomAccessFile raf = getRandomAccesFileFromFile(file);
		raf.seek(offset);
		raf.write(byteArray, 0, writeSize);
		raf.close();
	}

	public static void writeByteToFile(File file, int offset, byte byteToWrite)
			throws IOException {
		RandomAccessFile raf = getRandomAccesFileFromFile(file);
		raf.seek(offset);
		raf.write(byteToWrite);
		raf.close();
	}

	public static void writeBlockToFile(File file, int blockNr, int blockSize,
			byte[] data) throws IOException {
		writeByteArrayToFile(file, blockNr * blockSize, data, data.length);
	}

	public static byte[] getBlockFromFile(File file, int seekDistance,
			int blockSize) throws IOException {
		byte[] byteArray = new byte[blockSize];

		RandomAccessFile raf = getRandomAccesFileFromFileReadOnly(file);
		raf.seek(seekDistance);
		raf.read(byteArray);
		System.out.println("readed:" + new String(byteArray));

		return byteArray;
	}

	public static boolean removeFile(String location) {
		File file = new File(location);
		return file.delete();
	}
}