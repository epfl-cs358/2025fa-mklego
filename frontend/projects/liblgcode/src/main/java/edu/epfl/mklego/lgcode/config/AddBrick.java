package edu.epfl.mklego.lgcode.config;

import java.io.IOException;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.Serializable;
import edu.epfl.mklego.lgcode.format.StringSerializer;

public record AddBrick(byte brickId, String partName, byte colorId, int resistor) implements Serializable {
    public static final CommandKinds ADD_BRICK_COMMAND = CommandKinds.ADD_BRICK;

    public AddBrick (int brickId, String partName, int colorId, int resistor) {
        this((byte) brickId, partName, (byte) colorId, resistor);
    }

    @Override
    public void verify() throws ExceptionGroup {
        ExceptionGroup exceptions = new ExceptionGroup();

        if (resistor <= 0) {
            exceptions.addException(
                new IllegalArgumentException(
                    "Invalid resistor %s, it should have a strictly positive value."
                        .formatted(resistor)) );
        }

        exceptions.tryThrow();
    }

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write(
            "\t%s %s "
                .formatted(
                    ADD_BRICK_COMMAND.commandPrefix,
                    ((int) brickId) & 0xFF
                ).getBytes());
        StringSerializer.writeText(stream, partName);
        stream.write(
            " %s %s\n"
                .formatted(
                    ((int) colorId) & 0xFF,
                    resistor
                ).getBytes()
        );
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeBinary'");
    }
    
}
