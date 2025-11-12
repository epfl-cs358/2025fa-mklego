package edu.epfl.mklego.lgcode.format;

import java.io.IOException;
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
}
