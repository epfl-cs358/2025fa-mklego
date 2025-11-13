package edu.epfl.mklego.lgcode.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StringSerializer {
    public static String readBinary (InputStream stream) throws IOException {
        Integer val = IntSerializer.readBinary(stream);
        System.out.println("STRING SIZE " + val);
        if (val == null) return null;

        byte[] bytes = stream.readNBytes(val);
        if (bytes.length != val) return null;

        return new String(bytes);
    }
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
