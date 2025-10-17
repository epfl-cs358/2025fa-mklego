package edu.epfl.mklego.lgcode.format;

import java.io.IOException;
import java.io.OutputStream;

public interface Serializable {
    public void writeText   (OutputStream stream) throws IOException;
    public void writeBinary (OutputStream stream) throws IOException;

    public default void write (OutputStream stream, boolean writeText) throws IOException {
        if (writeText) this.writeText(stream);
        else this.writeBinary(stream);
    }
}
