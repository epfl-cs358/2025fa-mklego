package edu.epfl.mklego.lgcode.format;

import java.io.IOException;
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
}
