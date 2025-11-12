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
    public void slice (float[][][] weights) {

        int X = 22;
        int Y = 22; 

        int[][] previousLayer = createEmptyLayerArray(X, Y);

        for (int z = 0; z < weights.length; z++){
            previousLayer = simpleSlicer(weights[z], previousLayer, X, Y, z);
        }

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

    /**
     * calculates one layer of slicing
     * @param weights voxel weights for this layer
     * @param previousLayer block distributions for previous layer
     * @param X x dimentions of the board
     * @param Y y dimentions of the board
     * @param z z coordinate, currently used to index output files
     * @return the distribution of this layer
     */
    private static int[][] simpleSlicer(float[][] weights, int[][] previousLayer, int X, int Y, int z){

        float[][] w = {
        {-1.0f,-0.8901960784313725f,-0.803921568627451f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f,-1.0f},
        {-1.0f,0.5686274509803921f,0.7019607843137254f,-0.5215686274509803f,-1.0f,-0.15294117647058825f,0.5372549019607844f,0.3176470588235294f,-1.0f,-1.0f},
        {-0.41960784313725485f,0.9921568627450981f,1.0f,0.22352941176470598f,0.7490196078431373f,0.9921568627450981f,1.0f,1.0f,0.37254901960784315f,-1.0f},
        {0.3803921568627451f,1.0f,1.0f,0.9450980392156862f,1.0f,1.0f,1.0f,1.0f,0.7647058823529411f,-1.0f},
        {0.7882352941176471f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,1.0f,0.5921568627450979f,-1.0f},
        {0.9215686274509804f,1.0f,0.9921568627450981f,1.0f,1.0f,1.0f,1.0f,0.9843137254901961f,-0.5686274509803921f,-1.0f},
        {0.968627450980392f,1.0f,1.0f,1.0f,0.9921568627450981f,1.0f,1.0f,0.44313725490196076f,-1.0f,-1.0f},
        {0.968627450980392f,1.0f,1.0f,1.0f,0.9921568627450981f,1.0f,0.968627450980392f,-1.0f,-1.0f,-1.0f},
        {0.8509803921568628f,1.0f,1.0f,1.0f,0.9137254901960785f,1.0f,0.6000000000000001f,-1.0f,-1.0f,-1.0f},
        {0.09019607843137245f,0.9843137254901961f,1.0f,0.9137254901960785f,-0.19215686274509802f,0.2705882352941176f,-0.5529411764705883f,-1.0f,-1.0f,-1.0f}
    };

        List<Block> blockList = new ArrayList<Block>();
        Map<Pip, List<Integer>> blocksCoveringPip = new HashMap<>();

        addBlock(4, 2, weights, blockList, blocksCoveringPip, X, Y, previousLayer);
        addBlock(2, 4, weights, blockList, blocksCoveringPip, X, Y, previousLayer);
        /*addBlock(2, 2, weights, blockList, blocksCoveringPip, X, Y, previousLayer);
        addBlock(1, 2, weights, blockList, blocksCoveringPip, X, Y, previousLayer);
        addBlock(2, 1, weights, blockList, blocksCoveringPip, X, Y, previousLayer);*/

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
            System.out.println("Solution:");
            System.out.println("Objective value = " + objective.value());

            int[][] out = createEmptyLayerArray(X, Y);
            BufferedImage img = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);
            BufferedImage inimg = new BufferedImage(X, Y, BufferedImage.TYPE_INT_RGB);

            List<LegoPiece> pieces = new ArrayList<>();
            var LegoAssembly = new LegoAssembly(X, Y, pieces);

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
                pieces.add(new LegoPiece(i, i, null, legoPieceKind));
            }

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

                    System.err.print(v);
                    System.err.print(" ");
                }
                System.err.println(" ");
            }

            File outputFile1 = new File(String.format("layer %d.png", z));
            File outputFile2 = new File(String.format("input %d.png", z));
            try {
                ImageIO.write(img, "png", outputFile1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*try {
                ImageIO.write(inimg, "png", outputFile2);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            return out;
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
     */
    private static void addBlock(int X, int Y, float[][] weights, List<Block> blockList, Map<Pip, List<Integer>> blocksCoveringPip, 
    int xBound, int yBound, int[][] previousLayer){
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

                blockScore += addStructuralValue(coveredBlocks);
                blockScore += -Math.log((X+1) * (Y+1)) * 0.1f;

                int mainStubRow = x;
                int mainStubColumn = y;

                blockList.add(new Block(coveredPips, blockScore, X, Y, mainStubRow, mainStubColumn));
            }
        }
    }

    private static float addStructuralValue(Map<Integer, Integer> coveredBlocks){

        int numberOfCoveredBlocks = coveredBlocks.size();
        int minNumberOfCoveredPips = coveredBlocks.isEmpty() ? 0 : Collections.min(coveredBlocks.values());

        return (float)(Math.log(numberOfCoveredBlocks + 1) + Math.log(minNumberOfCoveredPips + 1));
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
}
