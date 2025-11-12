package edu.epfl.mklego.lgcode.print;

import java.io.IOException;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.Serializable;

public class Drop implements Serializable {
    public static final CommandKinds DROP_BRICK_COMMAND = CommandKinds.DROP_BRICK;

    @Override
    public void verify() throws ExceptionGroup {}

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write("\t%s\n".formatted(DROP_BRICK_COMMAND.commandPrefix).getBytes());
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        stream.write(new byte[] { (byte) DROP_BRICK_COMMAND.commandId });
    }
    
}
