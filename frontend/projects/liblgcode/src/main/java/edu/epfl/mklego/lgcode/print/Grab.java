package edu.epfl.mklego.lgcode.print;

import java.io.IOException;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.Serializable;

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

    
}
