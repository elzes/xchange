package xchange;

/*
 * Download class represents the download thread
 * 
 */
import java.io.*;
import java.net.*;

public class Download extends Thread {

    private GUI g;
    private Fileinfo f;
    private byte[] b;

    Download(GUI g, Fileinfo f) {
	this.g = g;
	this.f = f;
	// number of bytes read is, at most, equal to the length of b
	// allocate memory to buffer (yes, allocated on the heap)
	this.b = new byte[g.xc.BLOCKSIZE];

	if (Debug.DEBUG) {
	    System.out.println("New download of file " + f.name + " blocks= " + f.nr_of_blocks + " ip= " + f.ip);
	}
	try {
	    preAllocateFiles();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    
    /*
     * at start of a new download, disk space is pre-allocated
     * check is file exists, else create a new file of given size in xchange/shared
     * during download the file has exentsion .!xch, until file is complete
     * in addition, create info file in xchange/INFO with extension .!info
     *
     * in the info file each block is represented by a byte
     * if we use the chars we can view/edit the info file as text file :-)
     * '0' = block not avaiable, '1' = block available
     *
     */
    public void preAllocateFiles() throws Exception {
	File ftest = new File("xchange/shared/" + f.name + ".!xch");
	if (ftest.exists()) {
	    System.err.println("NOTIFY : file " + f.name + " found pre-allocated");
	    return;
	} else  {
	    // create a new file
	    RandomAccessFile file = new RandomAccessFile("xchange/shared/" + f.name + ".!xch", "rw");
	    // all bytes are set to zero by default
	    file.setLength(f.size);
	    file.close();
	    // create new info file
	    RandomAccessFile infoFile = new RandomAccessFile("xchange/INFO/" + f.name + ".!info", "rw");
	    // all bytes are set to zero by default
	    infoFile.setLength(f.nr_of_blocks);
	    // set all bytes to '0'
	    infoFile.seek(0);
	    for (int i = 0; i<f.nr_of_blocks; i++) {
		infoFile.writeByte((byte)'0');
	    }
	    infoFile.close();
	    
	    if (Debug.DEBUG) {
		System.out.println("Created new download file : shared/" + f.name +  ".!xch");
		System.out.println("Created new info file : xchange/INFO/" + f.name +  ".!info");
	    }
	}
    }

    /**
     * start downloading blocks into buffer this.b
     */

    // get a remote block from another peer and return size of block
    void getRemoteBlock(String fn, int block_nr, String ip, int size) throws Exception {
	// buffere socket streams for better performance
	BufferedInputStream bis;
	PrintWriter out;

	String line = null;
	int block_size;

	// make connection to other peer
	Socket ps = new Socket(ip, g.xc.port);

	// open a PrintWriter on socket, autoflush
	out = new PrintWriter(ps.getOutputStream(), true);
	// open a buffered reader on socket for raw data, give buffer size
	bis = new BufferedInputStream(ps.getInputStream(), g.xc.BLOCKSIZE);
	    
	// example : "GET hello.mp3 312\n"
	out.println("GET " + fn + " " + block_nr);

	/**
	 * try to read size bytes
	 * note : n should be the number of bytes actually read, but testing shows
	 * that n always equals BLOCKSIZE ...
	 */

	int n = -1;
	while (n < 1) {
	    n = bis.read(this.b, 0, size);
	};

	if (Debug.DEBUG) {
	    System.out.println("Received block with size " + size + " of remote file " + fn);
		    System.out.println("block contents :");
		    for (int i=0; i<size; i++) {
			System.out.print(this.b[i]);
		    }
		    System.out.println();
	}
    }

    public void run() {
	int size = g.xc.BLOCKSIZE;
	int rest;

	// for all blocks
	for (int i=0; i<f.nr_of_blocks; i++) {
	    if (i == (f.nr_of_blocks-1)) {
		// calculate size of the last block
		rest = (int)(f.size % g.xc.BLOCKSIZE);
		// if last block has BLOCKSIZE then rest = 0
		if (rest > 0) {
		    size = rest;
		}
	    }
	    if (g.stop_all_downloads) {
		if (Debug.DEBUG) {
		    System.out.println("Stop download of file " + f.name);
		}
		return;
	    }

	    try {
		// using this.b as buffer !
		getRemoteBlock(f.name, i, f.ip, size);
	    }
	    catch (Exception e) {
		e.printStackTrace();
		System.err.println("ERROR : unable to read block from socket");
		return;
	    }
	    
	    try {
		File file = new File("xchange/shared/" + f.name + ".!xch");
		if (! file.exists()) {
		    System.err.println("ERROR : file xchange/shared/" + f.name + ".!xch not found");
		    return;
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		raf.seek(i*g.xc.BLOCKSIZE);
		// last block usually smaller than BLOCKSIZE
		raf.write(this.b, 0, size);
		raf.close();
		    
		// and update info file
		File infoFile = new File("xchange/INFO/" + f.name + ".!info");
		if (! infoFile.exists()) {
		    System.err.println("ERROR : file xchange/INFO/" + f.name + ".!info not found");
		    return;
		}
		RandomAccessFile rafInfo = new RandomAccessFile(infoFile, "rw");
		rafInfo.seek(i);
		rafInfo.writeByte((byte)'1');
		rafInfo.close();
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	    if (Debug.DEBUG) {
		System.out.println("Downloaded new block of file xchange/shared/" + f.name);
	    }

	    // and update progress bar in GUI
	    // your code here ...
	    
	} // for all blocks

	// when downloading is finished, remove the !xch extension
	
	try {
	    File oldfile = new File("xchange/shared/" + f.name + ".!xch");
	    File newfile = new File("xchange/shared/" + f.name);
	    oldfile.renameTo(newfile);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
	if (Debug.DEBUG) {
	    System.out.println("Removed extension from file : xchange/shared/" + f.name +  ".!xch");
	}
    }
}
