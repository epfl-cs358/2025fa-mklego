// Load and process STL file (triangles → mesh → voxels)
package edu.epfl.mklego.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Responsible for loading an STL file and validating its contents.
 * Currently supports basic file existence and format checking.
 * Future versions can parse the mesh data or delegate to an external 3D library.
 */
public class StlLoader {

    /**
     * Loads an STL file from disk.
     *
     * @param filePath Path to the .stl file.
     * @return The raw file contents as a byte array (for now).
     * @throws IOException If the file doesn't exist or cannot be read.
     * @throws IllegalArgumentException If the file is not an STL file.
     */
    public static byte[] load(String filePath) throws IOException {
        File file = new File(filePath);

        // --- Check that the file exists and is readable ---
        if (!file.exists() || !file.isFile()) {
            throw new IOException("STL file not found: " + filePath);
        }

        // --- Check file extension ---
        if (!filePath.toLowerCase().endsWith(".stl")) {
            throw new IllegalArgumentException("Invalid file type. Expected .stl, got: " + filePath);
        }

        // --- Read file contents ---
        byte[] data = Files.readAllBytes(file.toPath());

        // --- Simple sanity check (ASCII vs binary STL header) ---
        // valid binary stl header have strictly less than 80 bytes, && ASCII headers always start with "solid"
        String header = new String(data, 0, Math.min(80, data.length));
        if (!header.trim().toLowerCase().startsWith("solid") && data.length < 84) {
            throw new IOException("File does not appear to be a valid STL: " + filePath);
        }

        System.out.println("Loaded STL file: " + file.getName() + " (" + data.length + " bytes)");
        return data;
    }
}
