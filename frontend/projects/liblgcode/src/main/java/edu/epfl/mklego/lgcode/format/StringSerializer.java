package edu.epfl.mklego.lgcode.format;

import java.io.IOException;
import java.io.OutputStream;

public class StringSerializer {
    public static void writeBinary (OutputStream stream, String buffer) throws IOException {
        byte[] bytes = buffer.getBytes();

        IntSerializer.writeBinary(stream, bytes.length);
        stream.write(bytes);
    }
    public static void writeText (OutputStream stream, String buffer) throws IOException {
        stream.write('"');
        for (byte val : buffer.getBytes()) {
            if (val == '\n') {
                stream.write('\\');
                stream.write('n');
                continue ;
            }
            if (val == '\t') {
                stream.write('\\');
                stream.write('t');
                continue ;
            }
            if (val == '"' || val == '\'' || val == '\\') {
                stream.write('\\');
            }
            stream.write(val);
        }
        stream.write('"');
    }
}
