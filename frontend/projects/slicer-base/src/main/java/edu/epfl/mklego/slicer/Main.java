package edu.epfl.mklego.slicer;

import java.io.File;
import java.io.FileInputStream;

import edu.epfl.mklego.objloader.Mesh;
import edu.epfl.mklego.objloader.ObjectLoader;
import edu.epfl.mklego.objloader.ObjectLoaderFactory;
import edu.epfl.mklego.slicer.voxelizer.Voxelizer;

public class Main {
    public static void main (String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar LegoBuilder.jar <path/to/file.stl>");
            return;
        }

        String inputPath = args[0];
        System.out.println("Loading STL file " + inputPath);

        try {
            // Step 1: Load mesh
            ObjectLoader loader = ObjectLoaderFactory.getObjectLoader(inputPath);
            Mesh mesh = loader.load(new FileInputStream(new File(inputPath)));
            System.out.println("Mesh loaded successfully.");

            // Step 2: Convert mesh to voxels
            Object voxels; Voxelizer.voxelize(mesh/*, 0.008 */);
            System.out.println("Voxelization complete.");

            // Step 3: Map voxels to LEGO blocks
            Object legoBlocks; new Slicer().slice(/* voxels */);
            System.out.println("LEGO mapping done.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
