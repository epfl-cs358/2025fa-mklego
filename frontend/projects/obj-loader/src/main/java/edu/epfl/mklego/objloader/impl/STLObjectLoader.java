package edu.epfl.mklego.objloader.impl;

import java.io.IOException;
import java.io.InputStream;

import edu.epfl.mklego.objloader.Mesh;
import edu.epfl.mklego.objloader.ObjectLoader;
import edu.epfl.mklego.objloader.Mesh.MeshVerificationError;

public class STLObjectLoader extends ObjectLoader {

    @Override
    public Mesh load(InputStream stream) throws MeshVerificationError, FileFormatException {
        /**
         * For a STL based mesh, all texture coordinates and colors
         * for internal triangles should be -1. Global color should
         * also be null. The way it works is you should give a list of points,
         * an empty list of texture coordinates, a list of normals
         * and an empty list of colors. The list of internal triangles
         * should then contain the index of the 3 points (p1, p2, p3)
         * in the points list and the index of the normal. One way
         * to implement it is the following :
         * - When you read a facet normal, p1, p2, p3, add the three points
         *   to the list of points and the normal to the list of normals,
         *   then build the internal triangle using the size of the points list
         *   and size of normals list.
         */
        // --- Read file contents ---
        byte[] data;
        try {
            data = stream.readAllBytes();
        } catch (IOException exception) {
            throw new FileFormatException(
                "STLObjectLoader", "IOException: " + exception.getMessage());
        }
        // --- Simple sanity check (ASCII vs binary STL header) ---
        // valid binary stl header have strictly less than 80 bytes, && ASCII headers always start with "solid"
        String header = new String(data, 0, Math.min(80, data.length));
        if (!header.trim().toLowerCase().startsWith("solid") && data.length < 84) {     
            throw new FileFormatException(
                "STLObjectLoader", "File does not appear to be a valid STL: ...");
        }

        return null;
    }
    
}
