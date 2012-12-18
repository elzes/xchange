package xchange;

/*
 * Download class represents the download thread
 * 
 */
import helpers.FileHelper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import xchange.gui.GUI;

public class Download extends Thread
{

    private static final String FILE_LOCATION = "xchange/shared/";
    private static final String FILE_INFO_LOCATION = "xchange/INFO/";
    private GUI gui;
    private Fileinfo fileinfo;
    private byte[] byteArray;

    public Download(GUI gui, Fileinfo fileinfo)
    {
        this.gui = gui;
        this.fileinfo = fileinfo;
        // number of bytes read is, at most, equal to the length of b
        // allocate memory to buffer (yes, allocated on the heap)
        this.byteArray = new byte[Xchange.BLOCKSIZE];

        if (Debug.DEBUG)
        {
            System.out.println("New download of file " + fileinfo.name + " blocks= " + fileinfo.nr_of_blocks + " ip= " + fileinfo.ip);
        }
        try
        {
            preAllocateFiles();
        } catch (IOException e)
        {
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
    public void preAllocateFiles() throws IOException
    {
        File file = FileHelper.loadFile(FILE_LOCATION + fileinfo.name + FileHelper.TMP_EXT);
        if (file.exists())
        {
            System.err.println("NOTIFY : file " + fileinfo.name + " found pre-allocated");
            return;
        } else
        {
            // create a new file
            FileHelper.createRandomAccesFileFromFile(file, fileinfo.size);

            // create new info file
            FileHelper.createRandomAccesFileFromFile(new File(FILE_INFO_LOCATION + fileinfo.name + FileHelper.INFO_EXT),
                    fileinfo.nr_of_blocks);

            if (Debug.DEBUG)
            {
                System.out.println("Created new download file : " + FILE_LOCATION + fileinfo.name + FileHelper.TMP_EXT);
                System.out.println("Created new info file : " + FILE_INFO_LOCATION + fileinfo.name + FileHelper.INFO_EXT);
            }
        }
    }

    /**
     * start downloading blocks into buffer this.b
     * 
     * @throws IOException
     * @throws UnknownHostException
     */

    // get a remote block from another peer and return size of block
    void getRemoteBlock(String fileName, int block_nr, String ip, int size) throws UnknownHostException, IOException
    {
        String line = null;
        int block_size;

        // make connection to other peer
        Socket ps = new Socket(ip, gui.xchange.port);

        // open a PrintWriter on socket, autoflush
        PrintWriter printWriter = new PrintWriter(ps.getOutputStream(), true);
        // open a buffered reader on socket for raw data, give buffer size
        BufferedInputStream bufferedInputStream = new BufferedInputStream(ps.getInputStream(), gui.xchange.BLOCKSIZE);

        // example : "GET hello.mp3 312\n"
        printWriter.println("GET " + fileName + " " + block_nr);

        /**
         * try to read size bytes note : n should be the number of bytes actually read, but testing shows that n always
         * equals BLOCKSIZE ...
         */

        int n = -1;
        while (n < 1)
        {
            n = bufferedInputStream.read(this.byteArray, 0, size);
        }

        if (Debug.DEBUG)
        {
            System.out.println("Received block with size " + size + " of remote file " + fileName);
            System.out.println("block contents :");
            for (int i = 0; i < size; i++)
            {
                System.out.print(this.byteArray[i]);
            }
            System.out.println();
        }
    }

    public void run()
    {
        int size = gui.xchange.BLOCKSIZE;
        int rest;

        // for all blocks
        for (int i = 0; i < fileinfo.nr_of_blocks; i++)
        {
            if (i == (fileinfo.nr_of_blocks - 1))
            {
                // calculate size of the last block
                rest = (int) (fileinfo.size % gui.xchange.BLOCKSIZE);
                // if last block has BLOCKSIZE then rest = 0
                if (rest > 0)
                {
                    size = rest;
                }
            }
            if (gui.stop_all_downloads)
            {
                if (Debug.DEBUG)
                {
                    System.out.println("Stop download of file " + fileinfo.name);
                }
                return;
            }

            try
            {
                // using this.b as buffer !
                getRemoteBlock(fileinfo.name, i, fileinfo.ip, size);
            } catch (Exception e)
            {
                e.printStackTrace();
                System.err.println("ERROR : unable to read block from socket");
                return;
            }

            try
            {
                File file = FileHelper.loadFile(FILE_LOCATION + fileinfo.name + FileHelper.TMP_EXT);
                if (!file.exists())
                {
                    System.err.println("ERROR : file " + FILE_LOCATION + fileinfo.name + FileHelper.TMP_EXT + " not found");
                    return;
                }
                FileHelper.writeByteArrayToFile(file, i * gui.xchange.BLOCKSIZE, this.byteArray, size);

                // and update info file
                File infoFile = new File(FILE_INFO_LOCATION + fileinfo.name + ".!info");
                if (!infoFile.exists())
                {
                    System.err.println("ERROR : file " + FILE_INFO_LOCATION + fileinfo.name + ".!info not found");
                    return;
                }

                FileHelper.writeByteToFile(infoFile, i, (byte) '1');
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            if (Debug.DEBUG)
            {
                System.out.println("Downloaded new block of file " + FILE_LOCATION + fileinfo.name);
            }
            // and update progress bar in GUI
            // your code here ...
            gui.updateProgress(fileinfo.name);

        } // for all blocks

        // when downloading is finished, remove the !xch extension
        FileHelper.renameTmpFile(FILE_LOCATION + fileinfo.name + FileHelper.TMP_EXT);

        if (Debug.DEBUG)
        {
            System.out.println("Removed extension from file : " + FILE_LOCATION + fileinfo.name + ".!xch");
        }
    }
}
