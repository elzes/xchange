package xchange;

/**
 * class represents 2 attributes of a file : name and size
 */

public class Fileinfo
{
    public String name;
    public long size;
    public int nr_of_blocks;
    public String ip;

    public Fileinfo(Xchange xc, String name, long size, String ip)
    {
        this.name = name;
        this.size = size;
        this.ip = ip;
        int b = (int) size / xc.BLOCKSIZE;
        if (size % xc.BLOCKSIZE != 0)
        {
            b++;
        }
        this.nr_of_blocks = b;
    }
}
