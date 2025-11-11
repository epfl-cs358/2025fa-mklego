package edu.epfl.mklego.slicer;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

import edu.epfl.mklego.objloader.Mesh;
import edu.epfl.mklego.objloader.ObjectLoader;
import edu.epfl.mklego.objloader.ObjectLoaderFactory;
import edu.epfl.mklego.slicer.voxelizer.Voxelizer;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main (String[] args) {

    try {
        BufferedImage img1 = ImageIO.read(new File("input 0.png"));
        BufferedImage img2 = ImageIO.read(new File("input 1.png"));
        
        int width = 22;
        int height = 22;

        // Create 3D weights array: z x width x height
        float[][][] weights = new float[2][width][height];

        // Convert images to weights
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = img1.getRGB(x, y);
                int gray1 = (rgb1 >> 16) & 0xFF;  // assuming grayscale
                weights[0][x][y] = gray1 / 127.5f - 1.0f;  // img1 is z=0

                int rgb2 = img2.getRGB(x, y);
                int gray2 = (rgb2 >> 16) & 0xFF;
                weights[1][x][y] = gray2 / 127.5f - 1.0f;  // img2 is z=1
            }
        }

        new Slicer().slice(weights);

    } catch (IOException e) {
        e.printStackTrace();
    }
        /*if (args.length < 1) {
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
            Object voxels; 
            float[][][] voxelWeights = Voxelizer.voxelize(mesh);
            System.out.println("Voxelization complete.");

            // Step 3: Map voxels to LEGO blocks
            Object legoBlocks; new Slicer().slice(voxelWeights);
            System.out.println("LEGO mapping done.");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }*/
    }

    private static float[][] imageToWeights(BufferedImage img) {
    int width = img.getWidth();
    int height = img.getHeight();
    float[][] weights = new float[width][height];

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            int rgb = img.getRGB(x, y);
            int gray = (rgb >> 16) & 0xFF;  // since R=G=B in grayscale
            // map 0..255 -> -1..1
            weights[x][y] = gray / 127.5f - 1.0f;
        }
    }
    return weights;
    }
}
