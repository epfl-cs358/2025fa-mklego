package edu.epfl.mklego.objloader;

import java.io.InputStream;

import edu.epfl.mklego.objloader.Mesh.MeshVerificationError;

public abstract class ObjectLoader {
    public static class FileFormatException extends Exception {
        public final String loader;
        public FileFormatException (String loader, String message) {
            super(message);

            this.loader = loader;
        }
    }

    public abstract Mesh load (InputStream stream) throws MeshVerificationError;
}
