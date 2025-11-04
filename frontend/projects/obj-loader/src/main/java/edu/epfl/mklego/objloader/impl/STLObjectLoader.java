package edu.epfl.mklego.objloader.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;


import edu.epfl.mklego.objloader.Mesh;
import edu.epfl.mklego.objloader.ObjectLoader;
import edu.epfl.mklego.objloader.Mesh.MeshVerificationError;

import javafx.geometry.Point3D;

public class STLObjectLoader extends ObjectLoader {

    @Override
    public Mesh load(InputStream stream) throws MeshVerificationError, FileFormatException {

        // loading all bytes from STL file
        byte[] data;
        try {
            data = stream.readAllBytes();
        } catch (IOException exception) {
            throw new FileFormatException(
                "STLObjectLoader", "IOException: " + exception.getMessage());
        }

        List<Point3D> points = new ArrayList<>();
        List<Point3D> normals = new ArrayList<>();

        // parsing binary STL, see format at https://firstmold.com/tips/stl-files/#h-binary-stl-files
        for (int i = 84; i < data.length; i += 50) {

            float nx = Float.intBitsToFloat(
                (data[i] & 0xFF) |
                (data[i + 1] & 0xFF) << 8 |
                (data[i + 2] & 0xFF) << 16 |
                (data[i + 3] & 0xFF) << 24
            );
            float ny = Float.intBitsToFloat(
                (data[i + 4] & 0xFF) |
                (data[i + 5] & 0xFF) << 8 |
                (data[i + 6] & 0xFF) << 16 |
                (data[i + 7] & 0xFF) << 24
            );
            float nz = Float.intBitsToFloat(
                (data[i + 8] & 0xFF) |
                (data[i + 9] & 0xFF) << 8 |
                (data[i + 10] & 0xFF) << 16 |
                (data[i + 11] & 0xFF) << 24
            );
            normals.add(new Point3D(nx, ny, nz));

            for (int c = 12; c <= 36; c += 12) {
                int j = i + c;
                float x = Float.intBitsToFloat(
                    (data[j] & 0xFF) |
                    (data[j + 1] & 0xFF) << 8 |
                    (data[j + 2] & 0xFF) << 16 |
                    (data[j + 3] & 0xFF) << 24
                );
                float y = Float.intBitsToFloat(
                    (data[j + 4] & 0xFF) |
                    (data[j + 5] & 0xFF) << 8 |
                    (data[j + 6] & 0xFF) << 16 |
                    (data[j + 7] & 0xFF) << 24
                );
                float z = Float.intBitsToFloat(
                    (data[j + 8] & 0xFF) |
                    (data[j + 9] & 0xFF) << 8 |
                    (data[j + 10] & 0xFF) << 16 |
                    (data[j + 11] & 0xFF) << 24
                );
                points.add(new Point3D(x, y, z));
            }
            
        }

        // verify if data length is coherent with number of triangles
        int nbTriangles = (data.length - 84) / 50; // 84 bytes header + 50 bytes per triangle
        int declaredTriangles = (data[80] & 0xFF) |((data[81] & 0xFF) << 8) |((data[82] & 0xFF) << 16) |((data[83] & 0xFF) << 24);

        if (!(normals.size() == nbTriangles && points.size() == nbTriangles * 3 && nbTriangles == declaredTriangles)) {
            throw new FileFormatException(
                "STLObjectLoader", "Incoherent number of triangles.");
        }
        
        // creating internal triangles only with points and normals, no texture coords or colors
        List<Mesh.InternalTriangle> triangles = new ArrayList<>();
        for (int i = 0; i < nbTriangles; i++) {
            triangles.add(new Mesh.InternalTriangle(
                i*3, i*3 + 1, i*3 + 2,
                -1, -1, -1,
                i,
                -1));
        }

        return new Mesh(null, points, new ArrayList<>(), normals, new ArrayList<>(), triangles);
    }
    
}
