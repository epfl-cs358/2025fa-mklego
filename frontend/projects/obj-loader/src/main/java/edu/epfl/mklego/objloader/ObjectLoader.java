package edu.epfl.mklego.objloader;

import java.io.InputStream;

import edu.epfl.mklego.objloader.Mesh.MeshVerificationError;

public abstract class ObjectLoader {
    public abstract Mesh load (InputStream stream) throws MeshVerificationError;
}
