package old.StorageServer;

public class StorageServerGetCommand extends StorageServerCommand
{

    private int blockNr;

    public StorageServerGetCommand(String line) throws InvalidCommandException
    {
        super(line);
        int blockNr;
        try
        {
            blockNr = Integer.parseInt(command[command.length - 1]);

        } catch (NumberFormatException e)
        {
            new InvalidCommandException("Invalid filesize");
            return;
        }
        setBlockNr(blockNr);
    }

    private void setBlockNr(int blockNr)
    {
        this.blockNr = blockNr;
    }

    public int getBlockNr()
    {
        return blockNr;
    }

    @Override
    protected int getCommandCountAfterFileName()
    {
        return 1;
    }

    @Override
    protected int getMinCommandLength()
    {
        return 3;
    }
}