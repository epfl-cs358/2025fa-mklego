package edu.epfl.mklego.lgcode.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.epfl.mklego.lgcode.ExceptionGroup;

public class SetSection implements Serializable {
    public static final CommandKinds SET_SECTION_COMMAND = CommandKinds.SET_SECTION;

    public static final SetSection CONFIG_SECTION = new SetSection("config", 0);
    public static final SetSection PRINT_SECTION  = new SetSection("print",  1);

    private final String name;
    private final int    uuid;
    private SetSection (String name, int uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    @Override
    public void verify() throws ExceptionGroup {}

    @Override
    public void writeText(OutputStream stream) throws IOException {
        stream.write(
            ".%s:\n"
                .formatted(name)
                .getBytes());
    }

    @Override
    public void writeBinary(OutputStream stream) throws IOException {
        stream.write(new byte[] {
            (byte) SET_SECTION_COMMAND.commandId,
            (byte) uuid
        });
    }

    public static SetSection readText (TextStream stream) throws ParseException, IOException {
        if (!stream.getCommand().startsWith("."))
            return null;
        
        if (stream.getCommand().equals("." + CONFIG_SECTION.name + ":"))
            return CONFIG_SECTION;
        if (stream.getCommand().equals("." + PRINT_SECTION.name + ":"))
            return PRINT_SECTION;

        throw new ParseException("Unknown section " + stream.getCommand());
    }
    public static SetSection readBinary (InputStream stream, int command) throws ParseException, IOException {
        if (command != CommandKindIds.SET_SECTION_CMD_ID)
            return null;
        
        int sectionId = stream.read();

        if (sectionId == CONFIG_SECTION.uuid)
            return CONFIG_SECTION;
        if (sectionId == PRINT_SECTION.uuid)
            return PRINT_SECTION;

            System.out.println(CONFIG_SECTION.uuid);
            System.out.println(PRINT_SECTION.uuid);
        throw new ParseException("Unknown section id " + sectionId);
    }
}
