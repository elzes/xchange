package nameServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NameServerClientHandler extends Thread
{

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private NameServer nameServer;

    public NameServerClientHandler(Socket client, NameServer nameServer)
    {
        this.client = client;
        this.nameServer = nameServer;
    }

    @Override
    public void run()
    {
        try
        {
            handle(client);
        } catch (Exception e)
        {
            e.printStackTrace();
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
            nameServer.handleAdd(line);
        }

        else if (line.startsWith("REMOVE"))
        {
            nameServer.handleRemove(line);
        }

        else if (line.startsWith("LIST"))
        {
            nameServer.handleList();
        }

        else
        {
            nameServer.handleError();
        }
    }
}
