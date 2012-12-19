package old.xchange;

/**
 * Class represents the i/f with storage server
 * in other words : handles interaction with storage server
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import old.helpers.FileHelper;
import old.xchange.gui.GUI;


public class XStorageServer
{
    GUI gui;

    public String ip = "";
    public final int port = 9002;

    BufferedReader bufferedReader;
    PrintWriter printWriter;

    private Socket socket;

    public XStorageServer(GUI gui)
    {
        this.gui = gui;
    }

    private void setupSocket() throws UnknownHostException, IOException
    {
        socket = new Socket(gui.storageServer.ip, port);
        // get socket input stream and open a BufferedReader on it
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        // get socket output stream and open a PrintWriter on it
        printWriter = new PrintWriter(socket.getOutputStream(), true);

    }

    /**
     * after router download is stopped, transfer the downloaded blocks from the storage server (GET)
     */

    public void TransferBlocks(String filename, int block_nr)
    {
        try
        {
            setupSocket();
            printWriter.println("GET " + filename + " " + block_nr);

            String line;
            // wait for response
            do
            {
                line = bufferedReader.readLine();
            } while (line == null);

            if (line.startsWith("OK"))
            {
                byte[] bytes = line.substring(3).getBytes();
                FileHelper.writeBlockToFile(new File(filename), block_nr, FileHelper.BLOCK_SIZE, bytes);
            } else
            {
                System.err.println("ERROR : Storageserver returned FAIL on GET request");
            }

            // close socket
            socket.close();
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * remove all files stored on storage server (after all blocks are transferred to client (REMOVE)
     */

    public void removeFile(String fileName)
    {
        String line = null;
        try
        {
            setupSocket();
            printWriter.println("REMOVE " + fileName);

            // wait for response
            do
            {
                line = bufferedReader.readLine();
            } while (line == null);

            // close socket
            socket.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if (Debug.DEBUG)
        {
            System.out.println("Received after REMOVE request :" + line);
        }
        if (line.equals("FAIL"))
        {
            System.err.println("ERROR : Storageserver returned FAIL on ADD request");
        }
    }
}
