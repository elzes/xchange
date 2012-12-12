package xchange;

/**
 * Class represents the i/f with storage server
 * in other words : handles interaction with storage server
 */

import java.io.*;
import java.net.*;

public class XStorageServer {
    GUI g;

    public String ip = "";
    public final int port = 9002;

    BufferedReader in;
    PrintWriter out;

    XStorageServer (GUI g) {
	this.g = g;
    }

    /**
     * after router download is stopped, transfer 
     * the downloaded blocks from the storage server (GET)
     */

    public void TransferBlocks(String filename, int block_nr) {
	
	// your code here ...
    }

    /**
     * remove all files stored on storage server (after all blocks are
     * transferred to client (REMOVE)
     */

    public void removeFiles() {
	String line = null;
	try {
	    // make connection to name server socket
	    Socket ss = new Socket(g.ss.ip, port);

	    // get socket input stream and open a BufferedReader on it
	    in = new BufferedReader(new InputStreamReader(ss.getInputStream()));
	    // get socket output stream and open a PrintWriter on it
	    out = new PrintWriter(ss.getOutputStream(), true);

	    out.println("REMOVE");

	    // wait for response
	    do {
		line = in.readLine();
	    } while (line == null);
	    
	    // close socket
	    ss.close();
	}
	catch (IOException e) {
	    e.printStackTrace();
	}

	if (Debug.DEBUG) {
	    System.out.println("Received after REMOVE request :" + line);
	}
	if (line.equals("FAIL")) {
	    System.err.println("ERROR : Storageserver returned FAIL on ADD request");
	}
    }
}
