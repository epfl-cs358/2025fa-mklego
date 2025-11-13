package edu.epfl.mklego.lgcode.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.Constants;
import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKindIds;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.ParseException;
import edu.epfl.mklego.lgcode.format.Serializable;
import edu.epfl.mklego.lgcode.format.TextStream;

public record PlateSize(int width, int height) implements Serializable {
    public static final CommandKinds PLATE_SIZE_COMMAND = CommandKinds.PLATE_SIZE;

    public void verify () throws ExceptionGroup {
        ExceptionGroup exceptions = new ExceptionGroup();

        if (width  <= 0 || width  > Constants.UNSIGNED_BYTE_MAX_VALUE) {
            exceptions.addException(
                new IllegalArgumentException(
                    "Invalid plate width %s, it should have values between 1 and 255"
                        .formatted(width)) );
        }
        if (height <= 0 || height > Constants.UNSIGNED_BYTE_MAX_VALUE) {
            exceptions.addException(
                new IllegalArgumentException(
                    "Invalid plate height %s, it should have values between 1 and 255"
                        .formatted(height)) );
        }

        exceptions.tryThrow();
    }

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write(
            "\t%s %s %s\n"
                .formatted(PLATE_SIZE_COMMAND.commandPrefix, width, height)
                .getBytes());
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        stream.write(new byte[] {
            (byte) PLATE_SIZE_COMMAND.commandId,
            (byte) width,
            (byte) height
        });
    }

    public static PlateSize readText (TextStream stream) throws ParseException, IOException {
        if (!stream.getCommand().equals(PLATE_SIZE_COMMAND.commandPrefix))
            return null;
    
        int width  = stream.readInt();
        int height = stream.readInt();
        return new PlateSize(width, height);
    }
    public static PlateSize readBinary (InputStream stream, int commandId) throws ParseException, IOException {
        if (commandId != CommandKindIds.PLATE_SIZE_CMD_ID)
            return null;
    
        int width  = stream.read();
        int height = stream.read();
        if (width  == -1) throw new ParseException("Could not read width");
        if (height == -1) throw new ParseException("Could not read height");
        
        return new PlateSize(width, height);
    }
}
