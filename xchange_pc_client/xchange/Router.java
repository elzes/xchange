package xchange;

/**
 * Class represents the router i/f 
 * in other words : handles router interaction
 */
import java.util.ArrayList;

public class Router
{

    public String ip = "";
    public final int port = 9003;

    // tell router to start (= take over) downloading list of files
    public void takeoverDownload(ArrayList<Fileinfo> downloadList)
    {
        // your code here ...
    }

    // tell router to stop downloading
    public void stopDownload()
    {
        // your code here ...
    }

    // push local info files to router
    public void pushRtInfoFiles()
    {
        // your code here ...
    }

    // get all info files from router and merge them with local info files
    public void getRtInfoFiles()
    {
        // your code here ...
    }

    // remove all info files from router
    public void removeRtInfoFiles()
    {
        // your code here ...
    }
}
