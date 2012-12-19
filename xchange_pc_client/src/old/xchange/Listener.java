package old.xchange;

/**
 * This class implements a server that is listening for incoming requests from other eers
 * handles the SEARCH en GET requests
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Listener extends Thread
{
    private Xchange xc;
    private byte[] b;

    // port and ip stored in Xchange

    // buffere socket streams for better performance
    BufferedReader in;
    PrintWriter out;
    BufferedOutputStream bos;

    public Listener(Xchange xc)
    {
        this.xc = xc;
        // allocate memory to buffer (yes, allocated on the heap)
        this.b = new byte[xc.BLOCKSIZE];

        if (Debug.DEBUG)
        {
            System.out.println("Listener thread started ...");
        }
    }

    public void run()
    {
        try
        {
            listenAndHandle();
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * create and listen on server socket
     * 
     */

    public void listenAndHandle() throws Exception
    {
        ServerSocket ss = new ServerSocket(xc.port);

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
        // open a BufferedReader on socket
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        // open a PrintWriter on socket, autoflush
        out = new PrintWriter(s.getOutputStream(), true);
        // open a buffered writer on socket for raw data, give buffer size
        bos = new BufferedOutputStream(s.getOutputStream(), xc.BLOCKSIZE);

        // get the message
        String line = null;
        do
        {
            line = in.readLine();
        } while (line == null);

        if (line.startsWith("SEARCH"))
        {
            handleSearch(line);
        }

        else if (line.startsWith("GET"))
        {
            handleGet(line);
        }

        else
        {
            handleError();
        }
    }

    private void handleSearch(String line) throws Exception
    {

        if (Debug.DEBUG)
        {
            System.out.println("Listener thread received : " + line);
        }
        // example : "SEARCH *\n"
        String[] s = line.split(" ");
        if (s.length != 2)
        {
            handleError();
            return;
        }
        // get rid of spaces end "/n"
        String pattern = s[1].trim();
        if (Debug.DEBUG)
        {
            System.out.println("Listener thread received pattern: " + pattern);
        }
        // get list of shared files and their sizes
        ArrayList<Fileinfo> files = xc.listSharedFiles(pattern);

        for (Fileinfo f : files)
        {
            out.print(f.name + " " + f.size + " ");
        }
        // send EOL
        out.println();

        if (Debug.DEBUG)
        {
            System.out.println("Listener replied with list of shared files and their sizes :");
            for (Fileinfo f : files)
            {
                System.out.println(f.name + " " + f.size);
            }
        }
    }

    private void handleGet(String line) throws Exception
    {
        // example : "GET hello.mp3 312\n"
        String[] s = line.split(" ");
        if (s.length != 3)
        {
            handleError();
            return;
        }
        String filename = s[1];
        int block_nr = Integer.parseInt(s[2]);

        try
        {
            // read block from local file into buffer this.b
            int size = xc.getLocalBlock(filename, block_nr, b);
            if (size > 0)
            {
                bos.write(b);
                bos.flush();
                if (Debug.DEBUG)
                {
                    System.out.println("Sent block with size " + size + " of local file " + filename + " to peer");
                    System.out.println("block contents :");
                    for (int i = 0; i < size; i++)
                    {
                        System.out.print(b[i]);
                    }
                    System.out.println();
                }
            } else
            {
                handleError();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            handleError();
        }
    }

    private void handleError() throws Exception
    {
        out.println("FAIL");
    }
}
