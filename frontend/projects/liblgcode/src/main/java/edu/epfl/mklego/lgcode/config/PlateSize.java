package edu.epfl.mklego.lgcode.config;

import java.io.IOException;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.Constants;
import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.Verifiable;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.Serializable;

public record PlateSize(int width, int height) implements Serializable, Verifiable {
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
    
}
