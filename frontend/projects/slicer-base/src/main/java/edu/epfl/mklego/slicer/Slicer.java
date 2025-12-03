package edu.epfl.mklego.slicer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.ortools.Loader;
import com.google.ortools.init.OrToolsVersion;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import edu.epfl.mklego.project.scene.entities.LegoPiece;
import edu.epfl.mklego.project.scene.entities.LegoPiece.LegoPieceKind;
import edu.epfl.mklego.project.scene.entities.LegoPiece.StdLegoPieceKind;


public class Slicer {
    public LegoAssembly slice (float[][][] weights) {

        int X = 22;
        int Y = 22; 

        int[][] previousLayer = createEmptyLayerArray(X, Y);
        List<LegoPiece> pieces = new ArrayList<>();

        for (int z = 0; z < weights.length; z++){
            layerReturn FOO = simpleSlicer(weights[z], previousLayer, X, Y, z, pieces);
            previousLayer = FOO.previousLayer;
        }

        return new LegoAssembly(X, Y, pieces);

        // simpleSlicer(weights[0], previousLayer, X, Y, 0);
    }

    private record Pip (
        int x, int y
    )
    {}

    private record Block (
        List<Pip> coveredPips,
        float score,
        int numberRows,
        int numberColumns,
        int mainStubRow,
        int mainStubColumn
    ){} 

    private record layerReturn (
        int[][] previousLayer,
        LegoAssembly assembly
    ){}

    /**
     * calculates one layer of slicing
     * @param weights voxel weights for this layer
     * @param previousLayer block distributions for previous layer
     * @param X x dimentions of the board
     * @param Y y dimentions of the board
     * @param z z coordinate, currently used to index output files
     * @return the distribution of this layer
     */
    private static layerReturn simpleSlicer(float[][] weights, int[][] previousLayer, int X, int Y, int z, List<LegoPiece> assembly){

        List<Block> blockList = new ArrayList<Block>();
        Map<Pip, List<Integer>> blocksCoveringPip = new HashMap<>();

        addBlock(4, 2, weights, blockList, blocksCoveringPip, X, Y, previousLayer, z);
        addBlock(2, 4, weights, blockList, blocksCoveringPip, X, Y, previousLayer, z);
        //addBlock(2, 2, weights, blockList, blocksCoveringPip, X, Y, previousLayer, z);
        /*aaddBlock(1, 2, weights, blockList, blocksCoveringPip, X, Y, previousLayer, z);
        addBlock(2, 1, weights, blockList, blocksCoveringPip, X, Y, previousLayer, z);*/

        // Create the linear solver with the SCIP backend.
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver SCIP");
            return null;
        }


        List<MPVariable> variables = new ArrayList<>();
        for (int i = 0; i < blockList.size(); i++) {
            MPVariable var = solver.makeIntVar(0.0, 1.0, "c_" + i);
            variables.add(var);
        }

        for (Map.Entry<Pip, List<Integer>> entry : blocksCoveringPip.entrySet()) {
            Pip pip = entry.getKey();
            List<Integer> blocksCoveringThisPip = entry.getValue();

            // Create constraint: sum of all covering blocks ≤ 1
            MPConstraint ct = solver.makeConstraint(0.0, 1.0, "pip_" + pip);

            for (int blockIndex : blocksCoveringThisPip) {
                ct.setCoefficient(variables.get(blockIndex), 1.0);
            }
        }

        MPObjective objective = solver.objective();
        for (int i = 0; i < blockList.size(); i++) {
            objective.setCoefficient(variables.get(i), blockList.get(i).score());
        }
        objective.setMaximization();

        final MPSolver.ResultStatus resultStatus = solver.solve();
        
        if (resultStatus == MPSolver.ResultStatus.OPTIMAL) {
            System.out.print("Solution of layer :");
            System.out.print(z);
            System.out.println("Objective value = " + objective.value());

            int[][] out = createEmptyLayerArray(X, Y);
            BufferedImage img = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
            BufferedImage inimg = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);

            List<LegoPiece> pieces = new ArrayList<>();

            for (int i = 0; i < blockList.size(); i++){
                if (Math.abs(variables.get(i).solutionValue() - 1.0) > 1e-4){
                    // variable is 0
                    continue;
                }
                Block b = blockList.get(i);
                for (Pip p : b.coveredPips()){
                    out[p.x][p.y] = i;
                }

                // save as LEGO block
                LegoPieceKind legoPieceKind = new StdLegoPieceKind(b.numberRows, b.numberColumns);
                pieces.add(new LegoPiece(b.mainStubRow, b.mainStubColumn, z, new javafx.scene.paint.Color(Math.random(), Math.random(), Math.random(), 1.), legoPieceKind));
            }

            assembly.addAll(pieces);

            //print output and save in image and lego assembly
            for (int x = 0; x < X; x++){
                for (int y = 0; y < Y; y++){

                    int v = out[x][y];
                    v++;            // needed to not generate black blocks
                    Color c = v == -1 ? Color.BLACK : new Color((v * 50) % 256, (v * 80) % 256, (v * 130) % 256);
                    img.setRGB(x, y, c.getRGB());
                    
                    float value = weights[x][y];  // -1 to 1
                    int intensity = (int)((value + 1) / 2 * 255);  // map -1..1 → 0..255
                    int rgb = (intensity << 16) | (intensity << 8) | intensity;  // set R=G=B=intensity
                    inimg.setRGB(x, y, rgb);

                    System.out.print(v);
                    System.out.print(" ");
                }
                System.out.println(" ");
            }

            /*File outputFile1 = new File(String.format("layer %d.png", z));
            File outputFile2 = new File(String.format("input %d.png", z));
            try {
                ImageIO.write(img, "png", outputFile1);
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            /*try {
                ImageIO.write(inimg, "png", outputFile2);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            return new layerReturn(out, null);
            } else {
            System.err.println("The problem does not have an optimal solution!");
            return null;
        }
    }

    /**
     * Adds blocks of the given dimentions, and saves the block index as it covers each pip. (Needed for the constraints in ILP)
     * @param X The X dimention of the Block
     * @param Y The Y dimention of the Block
     * @param weights The values of the pips in this 2d layer
     * @param blockList The list of blocks. The new block will be added to this
     * @param blocksCoveringPip Mapping from each pip to all the blocks that cover it. Needed for constraints in ILP
     * @param xBound The x size of the field to loop over
     * @param yBound the y size of the field to loop over
     * @param previousLayer a heatmap containing information on what coordinates have what block (first layer is all -1)
     * @param z what layer we are on. Used to punish floating bricks
     */
    private static void addBlock(int X, int Y, float[][] weights, List<Block> blockList, Map<Pip, List<Integer>> blocksCoveringPip, 
    int xBound, int yBound, int[][] previousLayer, int z){
        for (int x = 0; x < xBound - X + 1; x++){
            for (int y = 0; y < yBound - Y + 1; y++){
                float blockScore = 0;
                List<Pip> coveredPips = new ArrayList<>();
                Map<Integer, Integer> coveredBlocks = new HashMap<>();

                // loop over all pips to compute the score and save data for ILP
                for (int xx = 0; xx < X; xx++){
                    for (int yy = 0; yy < Y; yy++){
                        blockScore += weights[x + xx][y + yy];
                        Pip pip = new Pip(x + xx, y + yy);
                        coveredPips.add(pip);

                        blocksCoveringPip.computeIfAbsent(pip, k -> new ArrayList<>())
                            .add(blockList.size());

                        int coveringBlock = previousLayer[x + xx][y + yy];
                        if (coveringBlock != -1){
                            coveredBlocks.put(coveringBlock, coveredBlocks.getOrDefault(coveringBlock, 0)+1);
                        }
                    }
                }

                blockScore += addStructuralValue(coveredBlocks, z);
                blockScore += addLargePieceValue(X, Y);

                int mainStubRow = x;
                int mainStubColumn = y;

                blockList.add(new Block(coveredPips, blockScore, X, Y, mainStubRow, mainStubColumn));
            }
        }
    }

    private static double addStructuralValue(Map<Integer, Integer> coveredBlocks, int z){

        int numberOfCoveredBlocks = coveredBlocks.size();
        int minNumberOfCoveredPips = coveredBlocks.isEmpty() ? 0 : Collections.min(coveredBlocks.values());

        if (z != 0 && numberOfCoveredBlocks == 0) return -10.0;
        if (numberOfCoveredBlocks == 1) return 0;

        return (Math.log(numberOfCoveredBlocks + 1) + Math.log(minNumberOfCoveredPips + 1)) * 0.1f;
    }

    private static double addLargePieceValue(int X, int Y){
        return Math.log((X+1) * (Y+1)) * 0.1f;
    }

    private static int[][] createEmptyLayerArray(int X, int Y){
        int[][] out = new int[X][Y];
        for (int x = 0; x < X; x++){
            for (int y = 0; y < Y; y++){
                out[x][y] = -1;
            }
        }
        return out;
    }

    public static LegoAssembly assemblyTest(){
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

        return new Slicer().slice(weights);

    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
    }
}