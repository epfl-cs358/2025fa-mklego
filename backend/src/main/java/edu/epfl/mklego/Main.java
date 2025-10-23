package edu.epfl.mklego;

import edu.epfl.mklego.core.StlLoader;
import edu.epfl.mklego.core.Voxelizer;
import edu.epfl.mklego.core.LegoMapper;
import edu.epfl.mklego.core.GcodeGenerator;

public class Main {

    public static void main(String[] args) {
        // Check arguments
        if (args.length < 1) {
            System.out.println("Usage: java -jar LegoBuilder.jar <path/to/file.stl>");
            return;
        }

        String inputPath = args[0];
        System.out.println("🔧 Loading STL file: " + inputPath);

        try {
            // Step 1: Load mesh
            Object mesh = StlLoader.load(inputPath);
            System.out.println("Mesh loaded successfully.");

            // Step 2: Convert mesh to voxels
            Object voxels = Voxelizer.voxelize(mesh, 0.008);
            System.out.println("Voxelization complete.");

            // Step 3: Map voxels to LEGO blocks
            Object legoBlocks = LegoMapper.mapToLego(voxels);
            System.out.println("LEGO mapping done.");

            // Step 4: Generate LEGO-GCode
            GcodeGenerator.generate(legoBlocks, "output/build.lego");
            System.out.println("Build instructions generated at output/build.lego");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
