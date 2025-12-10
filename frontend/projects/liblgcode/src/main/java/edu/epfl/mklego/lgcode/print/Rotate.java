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

public record Rotate(byte rotation) implements Serializable {
    public static final CommandKinds ROTATE_COMMAND = CommandKinds.ROTATE;

    public Rotate (int rotation) {
        this((byte) rotation);
    }

    @Override
    public void verify() throws ExceptionGroup {
        ExceptionGroup exceptions = new ExceptionGroup();

        if (rotation != 0 && rotation != 1 && rotation != -1) {
            exceptions.addException(
                new IllegalArgumentException(
                    "Invalid rotation %s, it should be an integer between -1 and 1."
                        .formatted((int) rotation)) );
        }

        exceptions.tryThrow();
    }

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write("\t%s %s\n".formatted(ROTATE_COMMAND.commandPrefix, (int) rotation).getBytes());
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        stream.write(new byte[] { (byte) ROTATE_COMMAND.commandId, rotation });
    }

    public static Rotate readText (TextStream stream) throws ParseException, IOException {
        if (!stream.getCommand().equals(ROTATE_COMMAND.commandPrefix))
            return null;
    
        return new Rotate(stream.readInt());
    }
    public static Rotate readBinary (InputStream stream, int commandId) throws ParseException, IOException {
        if (commandId != CommandKindIds.ROTATE_CMD_ID)
            return null;
    
        int rotate = stream.read();
        if (rotate == -1) throw new ParseException("Could not parse rotation");
        return new Rotate(rotate);
    }
    
}
