/**
 * This class implements a separate application (server process)
 * handles the LIST, ADD and REMOVE commands
 * list of peers is stored in memory only
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import xchange.Debug;

public class NameServer
{

    private String ip = "";
    private final int port = 9001;
    private ArrayList<String> list;

    BufferedReader in;
    PrintWriter out;

    public NameServer()
    {
        list = new ArrayList<String>();
        if (Debug.DEBUG)
        {
            System.out.println("Nameserver started ...");
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
        // todo: make Nameserver multi-threaded, i.e. a new thread for each incoming client
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
        // get socket input stream and open a BufferedReader on it
        in = new BufferedReader(new InputStreamReader(s.getInputStream()));

        // get socket output stream and open a PrintWriter on it
        out = new PrintWriter(s.getOutputStream(), true);

        // get the message
        String line = null;
        do
        {
            line = in.readLine();
        } while (line == null);

        if (line.startsWith("ADD"))
        {
            handleAdd(line);
        }

        else if (line.startsWith("REMOVE"))
        {
            handleRemove(line);
        }

        else if (line.startsWith("LIST"))
        {
            handleList();
        }

        else
        {
            handleError();
        }
    }

    private void handleAdd(String line) throws Exception
    {
        if (Debug.DEBUG)
        {
            System.out.println("Nameserver received : " + line);
        }
        String[] s = line.split(" ");
        if (s.length != 2)
        {
            handleError();
            return;
        }
        String ip = s[1];
        if (!list.contains(ip))
        {
            list.add(ip);
        } else
        {
            if (Debug.DEBUG)
            {
                System.out.println("Nameserver : duplicate ip : " + ip);
            }
        }

        out.println("OK");
        if (Debug.DEBUG)
        {
            System.out.println("Nameserver : added " + ip);
        }
    }

    private void handleRemove(String line) throws Exception
    {
        String[] s = line.split(" ");
        if (s.length != 2)
        {
            handleError();
            return;
        }
        String ip = s[1];
        list.remove(ip);
        out.println("OK");
        if (Debug.DEBUG)
        {
            System.out.println("Nameserver : removed " + ip);
        }
    }

    private void handleList() throws Exception
    {
        if (Debug.DEBUG)
        {
            System.out.println("Nameserver received LIST request");
        }
        for (String ip : list)
        {
            out.print(ip + " ");
        }
        // send EOL
        out.println();
    }

    private void handleError() throws Exception
    {
        out.println("FAIL");
    }

    public static void main(String[] args) throws Exception
    {
        new NameServer();
    }
}
