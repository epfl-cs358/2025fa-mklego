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
    static final double MAX_SIZE_X = 100.0;
    static final double MAX_SIZE_Y = 100.0;
    static final double MAX_SIZE_Z = 100.0;

    @Override
    public Mesh load(InputStream stream) throws MeshVerificationError, FileFormatException {
        /**
         * For a STL based mesh, all texture coordinates and colors
         * for internal triangles should be -1. Global color should also be null. 
         * 
         * The way it works is you should give a list of points,
         * an empty list of texture coordinates, a list of normals
         * and an empty list of colors.
         * 
         * The list of internal triangles should then contain the index of the 3 points (p1, p2, p3)
         * in the points list and the index of the normal. 
         * One way to implement it is the following :
         *   When you read a facet normal, p1, p2, p3, add the three points
         *   to the list of points and the normal to the list of normals,
         *   then build the internal triangle using the size of the points list
         *   and size of normals list.
         */

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
        shiftPositive(points);
        scaleDown(points);
        return new Mesh(null, points, new ArrayList<>(), normals, new ArrayList<>(), triangles);
    }

    // Shifts coordinates to positive quadrant
    public void shiftPositive(List<Point3D> points) {
        if (points == null || points.isEmpty()) return;
        double minX = points.get(0).getX();
        double minY = points.get(0).getY();
        double minZ = points.get(0).getZ();
        for (Point3D p : points) {
            if (p.getX() < minX) minX = p.getX();
            if (p.getY() < minY) minY = p.getY();
            if (p.getZ() < minZ) minZ = p.getZ();
        }

        for (int i = 0; i < points.size(); i++) {
            Point3D p = points.get(i);
            points.set(i, new Point3D(p.getX() - minX, p.getY() - minY, p.getZ() - minZ));

        }
        return;
    }
    // Scales down the model to fit in the given max sizes (only if too big)
    // Assumes coordinates are already positive and alligned to origin
    public void scaleDown(List<Point3D> points) {
        if(points == null || points.isEmpty()) return;

        double maxX = points.get(0).getX();
        double maxY = points.get(0).getY();
        double maxZ = points.get(0).getZ();
        for (Point3D p : points) {
            if (p.getX() > maxX) maxX = p.getX();
            if (p.getY() > maxY) maxY = p.getY();
            if (p.getZ() > maxZ) maxZ = p.getZ();
        }
        double scaleX = maxX > 0 ? MAX_SIZE_X / maxX : Double.POSITIVE_INFINITY;
        double scaleY = maxY > 0 ? MAX_SIZE_Y / maxY : Double.POSITIVE_INFINITY;
        double scaleZ = maxZ > 0 ? MAX_SIZE_Z / maxZ : Double.POSITIVE_INFINITY;
        double scale = Math.min(scaleX, Math.min(scaleY, scaleZ));
        if (scale < 1.0) {
            for (int i = 0; i < points.size(); i++) {
                points.set(i, points.get(i).multiply(scale));
            }
        }
        return;
    }
    
}
