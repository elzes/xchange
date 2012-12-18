package StorageServer;

public abstract class StorageServerCommand
{

    private String fileName;
    protected String[] command;

    public StorageServerCommand(String line) throws InvalidCommandException
    {
        command = line.split(" ");
        if (command.length < getMinCommandLength())
        {
            throw new InvalidCommandException("invalid command length");
        }

        //merge filename if it contains <sp>
        String fileName = "";
        for (int i = 1; i <= command.length - getCommandCountAfterFileName(); i++)
        {
            fileName += command[i];
        }

        setFileName(fileName);
    }

    protected abstract int getCommandCountAfterFileName();

    protected abstract int getMinCommandLength();

    protected void setFileName(String fileName)
    {
        this.fileName = fileName;
    }

    public String getFileName()
    {
        return fileName;
    }
}