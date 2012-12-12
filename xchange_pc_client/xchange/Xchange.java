package xchange;

/**
 * Class represents the i/f with peers (other Xchange clients)
 * Note that downloads are handled in class Download and uploads in class Upload
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;

public class Xchange
{

    public String ip = "";
    public final int port = 9000;

    public final int BLOCKSIZE = 64 * 1024;

    public Xchange()
    {
        // start listening to incomming requests from peers
        Listener l = new Listener(this);
        l.start();
    }

    // get a block from local shared file, return size of block
    // first block has block_nr=0
    public int getLocalBlock(String fn, int block_nr, byte[] b) throws Exception
    {

        File file = new File("xchange/shared/" + fn);
        File tmpFile = new File("xchange/shared/" + fn + ".!xch");
        File infoFile = new File("xchange/INFO/" + fn + ".!info");

        int block_size = BLOCKSIZE;
        long start = BLOCKSIZE * block_nr;
        long end = BLOCKSIZE * (block_nr + 1);

        if (file.exists())
        {
            // complete file exists
            if (end > file.length())
            {
                block_size = (int) (file.length() - start);
            }

            RandomAccessFile raf = new RandomAccessFile(file, "r");
            raf.seek(start);
            raf.read(b, 0, block_size);
            raf.close();
            if (Debug.DEBUG)
            {
                System.out.println("block_size = " + block_size);
            }
            return block_size;
        } else
        {
            if (tmpFile.exists())
            {
                // (pre-allocated) file is not complete yet
                byte t;
                RandomAccessFile rafInfo = new RandomAccessFile(infoFile, "r");
                rafInfo.seek(block_nr);
                t = rafInfo.readByte();
                rafInfo.close();
                if (t == -1)
                {
                    System.err.println("ERROR : reached EOF in file xchange/INFO" + fn + ".!info");
                }
                // check if block exists
                if ((char) t == '1')
                {
                    if (end > tmpFile.length())
                    {
                        block_size = (int) (tmpFile.length() - start);
                    }

                    RandomAccessFile raf = new RandomAccessFile(tmpFile, "r");
                    raf.seek(start);
                    raf.read(b, 0, block_size);
                    raf.close();
                    if (Debug.DEBUG)
                    {
                        System.out.println("block_size = " + block_size);
                    }
                    return block_size;
                } else
                {
                    System.err.println("ERROR : block not present says info file xchange/INFO/" + fn + ".!info");
                    return 0;
                }
            } else
            {
                System.err.println("ERROR : file xchange/shared/" + fn + " not found");
                return 0;
            }
        }
    }

    /**
     * make a list of all the shared files and their sizes in xchange/shared in a reply to a SEARCH from another peer
     * this list is excluding files with extension .!xch and .!info
     */

    public ArrayList<Fileinfo> listSharedFiles(String pattern)
    {
        ArrayList<Fileinfo> al = new ArrayList<Fileinfo>();
        File[] files = new File("xchange/shared").listFiles();

        if (files == null)
        {
            System.err.println("ERROR : directory xchange/shared does not exist.");
            return al;
        }

        for (int i = 0; i < files.length; i++)
        {
            if (Debug.DEBUG)
            {
                System.out.println("Found local file : " + files[i].getName());
            }
            String tmpName = files[i].getName();
            if (pattern.equals("*") || (tmpName.indexOf(pattern) != -1))
            {
                if (tmpName.indexOf("!xch") == -1)
                {
                    al.add(new Fileinfo(this, files[i].getName(), files[i].length(), ip));
                }
            }
        }
        return al;
    }

    /**
     * make a list of all the shared files on the peer with specified ip address this list is excluding files with
     * extension .!xch and .!info
     */
    public ArrayList<Fileinfo> searchFile(String pattern, String ip)
    {
        ArrayList<Fileinfo> al = new ArrayList<Fileinfo>();
        BufferedReader in;
        PrintWriter out;

        String line = null;
        try
        {
            // make connection to other peer
            Socket ps = new Socket(ip, port);

            // get socket input stream and open a BufferedReader on it
            in = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            // get socket output stream and open a PrintWriter on it
            out = new PrintWriter(ps.getOutputStream(), true);

            out.println("SEARCH " + pattern);

            // wait for response
            do
            {
                line = in.readLine();
            } while (line == null);

            // close socket
            ps.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if (Debug.DEBUG)
        {
            System.out.println("Received after SEARCH request :" + line);
        }

        String[] s = { "" }; // avoid null-pointer exception
        if (line.startsWith("FAIL"))
        {
            System.err.println("ERROR : Peer " + ip + " returned FAIL on SEARCH request");
            return al;
        } else
        {
            // line does not include the '\n'
            // example : "hello.mp3 123456 helloWorld.txt 123"
            if (!line.equals(""))
            {
                s = line.split(" ");
                for (int i = 0; i < s.length; i = i + 2)
                {
                    al.add(new Fileinfo(this, s[i], Long.parseLong(s[i + 1]), ip));
                }
            }
            return al;

        }
    }
}