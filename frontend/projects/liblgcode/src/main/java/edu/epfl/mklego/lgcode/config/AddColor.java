package edu.epfl.mklego.lgcode.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;
import edu.epfl.mklego.lgcode.format.CommandKindIds;
import edu.epfl.mklego.lgcode.format.CommandKinds;
import edu.epfl.mklego.lgcode.format.ParseException;
import edu.epfl.mklego.lgcode.format.Serializable;
import edu.epfl.mklego.lgcode.format.StringSerializer;
import edu.epfl.mklego.lgcode.format.TextStream;

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
    
    public static AddColor readText (TextStream stream) throws ParseException, IOException {
        if (!stream.getCommand().equals(ADD_COLOR_COMMAND.commandPrefix))
            return null;
    
        int colorId = stream.readInt();
        int red     = stream.readInt();
        int green   = stream.readInt();
        int blue    = stream.readInt();
        int alpha   = stream.readInt();

        String name = stream.readEscapedString();
        String desc = stream.readEscapedString();
        return new AddColor(colorId, red, green, blue, alpha, name, desc);
    }
    
    public static AddColor readBinary (InputStream stream, int commandId) throws ParseException, IOException {
        if (commandId != CommandKindIds.ADD_COLOR_CMD_ID)
            return null;
    
        int colorId = stream.read();
        int red     = stream.read();
        int green   = stream.read();
        int blue    = stream.read();
        int alpha   = stream.read();

        String name = StringSerializer.readBinary(stream);
        String desc = StringSerializer.readBinary(stream);

        if (colorId == -1) throw new ParseException("Could not read colorId");
        if (red     == -1) throw new ParseException("Could not read red");
        if (green   == -1) throw new ParseException("Could not read green");
        if (blue    == -1) throw new ParseException("Could not read blue");
        if (alpha   == -1) throw new ParseException("Could not read alpha");

        if (name == null) throw new ParseException("Could not read name");
        if (desc == null) throw new ParseException("Could not read description");
        
        return new AddColor(colorId, red, green, blue, alpha, name, desc);
    }
}
