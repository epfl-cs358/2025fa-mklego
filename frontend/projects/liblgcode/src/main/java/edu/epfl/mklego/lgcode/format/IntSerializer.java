package edu.epfl.mklego.lgcode.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IntSerializer {
    public static void writeBinary (OutputStream stream, int data) throws IOException {
        stream.write(new byte[] {
            (byte) ((data >> 24) & 0xFF),
            (byte) ((data >> 16) & 0xFF),
            (byte) ((data >> 8 ) & 0xFF),
            (byte) ((data      ) & 0xFF)
        });
    }
    public static Integer readBinary (InputStream stream) throws IOException {
        int i3 = stream.read();
        int i2 = stream.read();
        int i1 = stream.read();
        int i0 = stream.read();

        if (i0 == -1) return null;
    
        return (
            (i3 << 24) | (i2 << 16) | (i1 << 8) | i0
        );
    }
}
