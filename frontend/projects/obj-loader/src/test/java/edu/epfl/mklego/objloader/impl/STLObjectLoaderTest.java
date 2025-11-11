package edu.epfl.mklego.objloader.impl;

import edu.epfl.mklego.objloader.Mesh;
import edu.epfl.mklego.objloader.ObjectLoader.FileFormatException;
import edu.epfl.mklego.objloader.Mesh.MeshVerificationError;

import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class STLObjectLoaderTest {

    @Test
    void testLoadAndPrintMesh() throws Exception {
        STLObjectLoader loader = new STLObjectLoader();

        // Load a test STL file from resources
        try (InputStream stream = getClass().getResourceAsStream("/Cube.stl")) {
            if (stream == null) {
                throw new IllegalStateException("Could not find sample.stl in test resources!");
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
}
