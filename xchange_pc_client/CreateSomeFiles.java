/**
 * creates some test files 
 */

import java.io.*;

public class CreateSomeFiles {
    
    private final int BLOCKSIZE = 64*1024; 
    private int i;

    CreateSomeFiles() {
	try {
	    RandomAccessFile raf = new RandomAccessFile("one-block-a.mp3", "rw");
	    for(i = 0; i < 2; i++) {
		raf.writeByte((byte)'a');
	    }
	    raf.close();

	    raf = new RandomAccessFile("one-block-b.mp3", "rw");
	    for(i = 0; i < BLOCKSIZE; i++) {
		raf.writeByte((byte)'b');
	    }
	    raf.close();

	    raf = new RandomAccessFile("two-block-a.mp3", "rw");
	    for(i = 0; i < BLOCKSIZE+1; i++) {
		raf.writeByte((byte)'c');
	    }
	    raf.close();

	    raf = new RandomAccessFile("two-block-b.mp3", "rw");
	    for(i = 0; i < 2*BLOCKSIZE; i++) {
		raf.writeByte((byte)'d');
	    }
	    raf.close();

	    raf = new RandomAccessFile("three-block.mp3", "rw");
	    for(i = 0; i < 2*BLOCKSIZE+1; i++) {
		raf.writeByte((byte)'e');
	    }
	    raf.close();
	}
	catch (IOException e) {
	    e.printStackTrace();
	    
	}
    }


    public static void main(String[] args) {
	new CreateSomeFiles();
    }
}
