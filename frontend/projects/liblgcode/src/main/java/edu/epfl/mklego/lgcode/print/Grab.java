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

public record Grab(byte brickId, byte attachmentId) implements Serializable {
    public static final CommandKinds GRAB_BRICK_COMMAND = CommandKinds.GRAB_BRICK;

    public Grab (int brickId, int attachmentId) {
        this((byte) brickId, (byte) attachmentId);
    }

    @Override
    public void verify() throws ExceptionGroup {}

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write("\t%s %s %s\n"
            .formatted(GRAB_BRICK_COMMAND.commandPrefix, brickId, attachmentId)
            .getBytes());
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        stream.write(new byte[] { (byte) GRAB_BRICK_COMMAND.commandId, brickId, attachmentId });
    }

    public static Grab readText (TextStream stream) throws ParseException, IOException {
        if (!stream.getCommand().equals(GRAB_BRICK_COMMAND.commandPrefix))
            return null;
    
        int brickId = stream.readInt();
        int attachmentId = stream.readInt();

        return new Grab(brickId, attachmentId);
    }
    public static Grab readBinary (InputStream stream, int commandId) throws ParseException, IOException {
        if (commandId != CommandKindIds.GRAB_BRICK_CMD_ID)
            return null;
    
        int brickId = stream.read();
        int attachmentId = stream.read();

        if (brickId == -1) throw new ParseException("Could not read brickId");
        if (attachmentId == -1) throw new ParseException("Could not read attachmentId");
        
        return new Grab(brickId, attachmentId);
    }
    
}
