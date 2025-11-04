package edu.epfl.mklego.objloader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.shape.TriangleMesh;

public class Mesh {
    public static record InternalTriangle (
        int p1, int p2, int p3,
        int t1, int t2, int t3,
        int normal, int color) {}
    public static record Triangle (
        Point3D p1, Point3D p2, Point3D p3,
        Point2D t1, Point2D t2, Point2D t3,
        Point3D normal, Color color) {}
    public static class MeshVerificationError extends Exception {
        public MeshVerificationError (String message) {
            super(message);
        }
    }

    private final Color globalColor;

    private final List<Point3D> points;
    private final List<Point2D> texCoords;
    private final List<Point3D> normals;
    private final List<Color>   colors;

    private final List<InternalTriangle> triangles;
    
    private List<Triangle> trianglesOutput = null;
    private Point3D getPoint (int id) {
        if (id < 0 || id >= points.size()) return null;
        return points.get(id);
    }
    private Point2D getTexCoord (int id) {
        if (id < 0 || id >= texCoords.size()) return null;
        return texCoords.get(id);
    }
    private Point3D getNormal (int id) {
        if (id < 0 || id >= normals.size()) return null;
        return normals.get(id);
    }
    private Color getColor (int id) {
        if (id < 0 || id >= colors.size()) return globalColor;
        return colors.get(id);
    }

    private void computeTrianglesOutput () {
        if (trianglesOutput != null) return ;

        List<Triangle> triangles = new ArrayList<>();
        for (InternalTriangle trig : this.triangles) {
            triangles.add(
                new Triangle(
                    getPoint(trig.p1), getPoint(trig.p2), getPoint(trig.p3), 
                    getTexCoord(trig.t1), getTexCoord(trig.t2), getTexCoord(trig.t3), 
                    getNormal(trig.normal), getColor(trig.color)));
        }

        this.trianglesOutput = triangles;
    }

    public List<Triangle> triangles () {
        computeTrianglesOutput();
        return Collections.unmodifiableList(trianglesOutput);
    }
    public TriangleMesh asTriangleMesh () {
        // TODO
        return null;
    }

    public void verify () throws MeshVerificationError {
        for (InternalTriangle trig : this.triangles) {
            if (getPoint(trig.p1) == null || getPoint(trig.p2) == null || getPoint(trig.p3) == null) {
                throw new MeshVerificationError("Invalid set of points.");
            }
        }
    }
    public Mesh(Color globalColor, List<Point3D> points, List<Point2D> texCoords, List<Point3D> normals,
            List<Color> colors, List<InternalTriangle> triangles)
            throws MeshVerificationError {
        this.globalColor = globalColor;
        this.points      = points;
        this.texCoords   = texCoords;
        this.normals     = normals;
        this.colors      = colors;
        this.triangles   = triangles;

        verify();
    }
}
