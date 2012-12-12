import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import xchange.Debug;

/**
 * This class implements a separate application (server process) handles the GET, POST and REMOVE commands blocks are
 * stored in ./files directory
 */

public class StorageServer
{

    private String ip = "";
    private final int port = 9002;
    private final int BLOCKSIZE = 64 * 1024;

    // un-buffered socket stream (flushing has no purpose)
    InputStream dis;
    OutputStream dos;

    // buffered socket stream
    BufferedReader in;
    PrintWriter out;

    public StorageServer()
    {
        if (Debug.DEBUG)
        {
            System.out.println("Storageserver started ...");
        }
        // check if ./files directory exists
        File file = new File("./files");
        if (!file.exists())
        {
            System.out.println("Please create directory ./files with r+w access.");
            System.err.println("ERROR : directory ./files does not exist.");
            System.exit(0);
        }

        try
        {
            listenAndHandle();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * create and listen on server socket
     * 
     */

    public void listenAndHandle() throws Exception
    {
        ServerSocket ss = new ServerSocket(port);

        // listen for incoming connections

        while (true)
        {
            Socket cs = ss.accept();
            // handle a single request
            handle(cs);
            // close client socket and resume listening
            cs.close();
        }
    }

    /**
     * handle incoming messages all messages are strings ending on "\n"
     */
    private void handle(Socket s) throws Exception
    {
        // get socket i/o stream
        dis = s.getInputStream();
        dos = s.getOutputStream();

        // open a BufferedReader on it
        in = new BufferedReader(new InputStreamReader(dis));
        // open a PrintWriter on it
        out = new PrintWriter(dos, true);

        // get the message
        String line = null;
        do
        {
            line = in.readLine();
        } while (line == null);

        if (line.startsWith("POST"))
        {
            handlePost(line);
        }

        else if (line.startsWith("GET"))
        {
            handleGet(line);
        }

        else if (line.startsWith("REMOVE"))
        {
            handleRemove(line);
        }

        else
        {
            handleError();
        }
    }

    private void handlePost(String line) throws Exception
    {
        if (Debug.DEBUG)
        {
            System.out.println("Storageserver received : " + line);
        }

        // your code here

        out.println("OK");
        if (Debug.DEBUG)
        {
            System.out.println("Storageserver : added block to file");
        }
    }

    private void handleGet(String line) throws Exception
    {
        if (Debug.DEBUG)
        {
            System.out.println("Storageserver received : " + line);
        }

        // your code here

        if (Debug.DEBUG)
        {
            System.out.println("Storageserver : sent block from file");
        }
    }

    private void handleRemove(String line) throws Exception
    {
        if (Debug.DEBUG)
        {
            System.out.println("Storageserver received : " + line);
        }

        // this is a stub ! your code here

        out.println("OK");
        if (Debug.DEBUG)
        {
            System.out.println("Storageserver : all files removed !");
        }
    }

    private void handleError() throws Exception
    {
        out.println("FAIL");
    }

    public static void main(String[] args) throws Exception
    {
        new StorageServer();
    }
}
