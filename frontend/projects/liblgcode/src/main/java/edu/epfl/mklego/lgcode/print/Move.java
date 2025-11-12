package edu.epfl.mklego.lgcode.print;

import java.io.IOException;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.Serializable;

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
    
}
