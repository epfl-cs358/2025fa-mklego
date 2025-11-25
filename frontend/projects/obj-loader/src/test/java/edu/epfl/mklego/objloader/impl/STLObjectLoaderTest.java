package edu.epfl.mklego.objloader.impl;

import edu.epfl.mklego.objloader.Mesh;
import edu.epfl.mklego.objloader.ObjectLoader.FileFormatException;
import edu.epfl.mklego.objloader.Mesh.MeshVerificationError;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point3D;

import static org.junit.jupiter.api.Assertions.*;

public class STLObjectLoaderTest {

    // Adjust these to match the constants in STLObjectLoader
    private static final double MAX_SIZE_X = 100.0;
    private static final double MAX_SIZE_Y = 100.0;
    private static final double MAX_SIZE_Z = 100.0;
    private static final double EPS = 1e-9;

    @Test
    void testLoadAndPrintMesh() throws Exception {
        STLObjectLoader loader = new STLObjectLoader();

        // Load a test STL file from resources
        try (InputStream stream = getClass().getResourceAsStream("/Cube.stl")) {
            if (stream == null) {
                throw new IllegalStateException("Could not find Cube.stl in test resources!");
            }

            Mesh mesh = loader.load(stream);

            System.out.println("STL file loaded successfully!");
            System.out.println("Number of triangles: " + mesh.triangles().size());

            // Print the first few triangles for inspection
            mesh.triangles().stream()
                .limit(3)
                .forEach(t -> System.out.println(
                    "Triangle: " + t.p1() + " | " + t.p2() + " | " + t.p3() +
                    " | normal=" + t.normal()));
        } catch (FileFormatException | MeshVerificationError e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    void shiftPositive_makesAllCoordinatesNonNegative_andPreservesShape() {
        STLObjectLoader loader = new STLObjectLoader();

        // Some arbitrary points, including negative coordinates
        List<Point3D> points = new ArrayList<>();
        points.add(new Point3D(-10, 5, 0));
        points.add(new Point3D(0, -3, 7));
        points.add(new Point3D(4, 2, -8));

        // Deep copy for distance comparison
        List<Point3D> original = new ArrayList<>();
        for (Point3D p : points) {
            original.add(new Point3D(p.getX(), p.getY(), p.getZ()));
        }

        loader.shiftPositive(points);

        // 1) All coordinates should be >= 0
        for (Point3D p : points) {
            assertTrue(p.getX() >= -EPS, "X should be non-negative");
            assertTrue(p.getY() >= -EPS, "Y should be non-negative");
            assertTrue(p.getZ() >= -EPS, "Z should be non-negative");
        }

        // 2) Distances between points should be preserved (pure translation)
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double distBefore = original.get(i).distance(original.get(j));
                double distAfter  = points.get(i).distance(points.get(j));
                assertEquals(distBefore, distAfter, EPS,
                        "Distances between points must be preserved by shiftPositive");
            }
        }
    }

    @Test
    void scaleDown_scalesUniformly_andKeepsPointsWithinBounds_whenTooBig() {
        STLObjectLoader loader = new STLObjectLoader();

        // Build a "too big" shape so scaling is actually applied
        List<Point3D> points = new ArrayList<>();
        points.add(new Point3D(0, 0, 0));
        points.add(new Point3D(200, 0, 0));
        points.add(new Point3D(0, 150, 0));
        points.add(new Point3D(0, 0, 300));

        // shiftPositive is assumed to be called before scaleDown in real usage
        loader.shiftPositive(points);

        // Copy after shift, before scaling, to compare distances
        List<Point3D> beforeScale = new ArrayList<>();
        for (Point3D p : points) {
            beforeScale.add(new Point3D(p.getX(), p.getY(), p.getZ()));
        }

        loader.scaleDown(points);

        // 1) All coordinates must be within [0, MAX_SIZE_*]
        for (Point3D p : points) {
            assertTrue(p.getX() >= -EPS && p.getX() <= MAX_SIZE_X + EPS, "X out of bounds");
            assertTrue(p.getY() >= -EPS && p.getY() <= MAX_SIZE_Y + EPS, "Y out of bounds");
            assertTrue(p.getZ() >= -EPS && p.getZ() <= MAX_SIZE_Z + EPS, "Z out of bounds");
        }

        // 2) Scaling must be uniform: all distances scaled by the same factor
        // Compute scale factor from one pair
        double dBeforeRef = beforeScale.get(0).distance(beforeScale.get(1));
        double dAfterRef  = points.get(0).distance(points.get(1));
        double scaleRef   = dAfterRef / dBeforeRef;

        // Check for all other pairs (where distance before != 0)
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                double dBefore = beforeScale.get(i).distance(beforeScale.get(j));
                if (dBefore < EPS) continue; // skip coincident points
                double dAfter  = points.get(i).distance(points.get(j));
                double scale   = dAfter / dBefore;
                assertEquals(scaleRef, scale, 1e-6,
                        "Scale must be uniform across all point pairs");
            }
        }
    }

    @Test
    void scaleDown_doesNothing_whenAlreadyWithinBounds() {
        STLObjectLoader loader = new STLObjectLoader();

        // Already small model: max extent less than MAX_SIZE_*
        List<Point3D> points = new ArrayList<>();
        points.add(new Point3D(0, 0, 0));
        points.add(new Point3D(10, 5, 2));
        points.add(new Point3D(3, 8, 1));

        // shift first (as usual)
        loader.shiftPositive(points);

        // Copy for comparison
        List<Point3D> before = new ArrayList<>();
        for (Point3D p : points) {
            before.add(new Point3D(p.getX(), p.getY(), p.getZ()));
        }

        loader.scaleDown(points);

        // Since the object already fits in the bounds, scaleDown should not change it
        for (int i = 0; i < points.size(); i++) {
            Point3D pBefore = before.get(i);
            Point3D pAfter  = points.get(i);
            assertEquals(pBefore.getX(), pAfter.getX(), EPS);
            assertEquals(pBefore.getY(), pAfter.getY(), EPS);
            assertEquals(pBefore.getZ(), pAfter.getZ(), EPS);
        }
    }
}
