package edu.epfl.mklego.lgcode.print;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKindIds;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.ParseException;
import edu.epfl.mklego.lgcode.format.Serializable;
import edu.epfl.mklego.lgcode.format.TextStream;

public record Move(byte x, byte y, byte z) implements Serializable {
    public static final CommandKinds MOVE_COMMAND = CommandKinds.MOVE;

    public Move (int x, int y, int z) {
        this((byte) x, (byte) y, (byte) z);
    }

    @Override
    public void verify() throws ExceptionGroup {}

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write("\t%s %s %s %s\n"
            .formatted(
                MOVE_COMMAND.commandPrefix,
                ((int) x) & 0xFF,
                ((int) y) & 0xFF,
                ((int) z) & 0xFF)
            .getBytes()
        );
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        stream.write(new byte[] { (byte) MOVE_COMMAND.commandId, x, y, z });
    }

    public static Move readText (TextStream stream) throws ParseException, IOException {
        if (!stream.getCommand().equals(MOVE_COMMAND.commandPrefix))
            return null;
    
        int x = stream.readInt();
        int y = stream.readInt();
        int z = stream.readInt();
        
        return new Move(x, y, z);
    }
    public static Move readBinary (InputStream stream, int commandId) throws ParseException, IOException {
        if (commandId != CommandKindIds.MOVE_CMD_ID)
            return null;
    
        int x = stream.read();
        int y = stream.read();
        int z = stream.read();

        if (x == -1) throw new ParseException("Could not parse x");
        if (y == -1) throw new ParseException("Could not parse y");
        if (z == -1) throw new ParseException("Could not parse z");    
        
        return new Move(x, y, z);
    }
    
}
