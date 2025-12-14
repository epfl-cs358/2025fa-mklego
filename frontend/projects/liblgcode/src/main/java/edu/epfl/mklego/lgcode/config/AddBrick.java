package edu.epfl.mklego.lgcode.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKindIds;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.IntSerializer;
import edu.epfl.mklego.lgcode.format.ParseException;
import edu.epfl.mklego.lgcode.format.Serializable;
import edu.epfl.mklego.lgcode.format.StringSerializer;
import edu.epfl.mklego.lgcode.format.TextStream;

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
        stream.write(new byte[] { (byte) ADD_BRICK_COMMAND.commandId, brickId });
        StringSerializer.writeBinary(stream, partName);
        stream.write(new byte[] { colorId });
        IntSerializer.writeBinary(stream, resistor);
    }
    
    public static AddBrick readText (TextStream stream) throws ParseException, IOException {
        if (!stream.getCommand().equals(ADD_BRICK_COMMAND.commandPrefix))
            return null;
    
        int brickId = stream.readInt();
        String partName = stream.readEscapedString();
        int colorId = stream.readInt();
        int resistor = stream.readInt();

        return new AddBrick(brickId, partName, colorId, resistor);
    }
    public static AddBrick readBinary (InputStream stream, int commandId) throws ParseException, IOException {
        if (commandId != CommandKindIds.ADD_BRICK_CMD_ID)
            return null;
    
        int brickId = stream.read();
        String partName = StringSerializer.readBinary(stream);
        int colorId = stream.read();
        Integer resistor = IntSerializer.readBinary(stream);

        if (brickId == -1) throw new ParseException("Could not read brickId");
        if (partName == null) throw new ParseException("Could not read partName");
        if (colorId == -1) throw new ParseException("Could not read colorId");
        if (resistor == null) throw new ParseException("Could not read resistor");

        return new AddBrick(brickId, partName, colorId, resistor);
    }
    
}
