package edu.epfl.mklego.lgcode.config;

import java.io.IOException;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.Serializable;
import edu.epfl.mklego.lgcode.format.StringSerializer;

public record AddColor(
        byte colorId,
        byte red, byte green, byte blue, byte alpha,
        String name, String description) implements Serializable {
    public static final CommandKinds ADD_COLOR_COMMAND = CommandKinds.ADD_COLOR;

    public AddColor (int colorId, int red, int green, int blue, int alpha, String name, String description) {
        this((byte) colorId, (byte) red, (byte) green, (byte) blue, (byte) alpha, name, description);
    }

    @Override
    public void verify() throws ExceptionGroup {}

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write(
            "\t%s %s %s %s %s %s "
                .formatted(
                    ADD_COLOR_COMMAND.commandPrefix,
                    ((int) colorId) & 0xFF,
                    ((int) red) & 0xFF,
                    ((int) green) & 0xFF,
                    ((int) blue) & 0xFF,
                    ((int) alpha) & 0xFF
                ).getBytes());
        
        StringSerializer.writeText(stream, name);
        stream.write(' ');
        StringSerializer.writeText(stream, description);
        stream.write('\n');
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        stream.write(new byte[] { 
            (byte) ADD_COLOR_COMMAND.commandId,
            colorId, red, green, blue, alpha });
        
        StringSerializer.writeBinary(stream, name);
        StringSerializer.writeBinary(stream, description);
    }
    
}
