package edu.epfl.mklego.slicer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.ortools.Loader;
import com.google.ortools.init.OrToolsVersion;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

public class Slicer {
    public void slice (float[][][] weights) {
        for (int z = 0; z < weights.length; z++){
            simpleSlicer(weights[z]);
        }
    }

    private record Pip (
        int x, int y
    )
    {}

    private record block (
        List<Pip> coveredPips,
        float score
    ){} 

    private static void simpleSlicer(float[][] weights){

        // dimentions of the board
        int X = 22;
        int Y = 22;

        List<block> blockList = new ArrayList<block>();
        Map<Pip, List<Integer>> blocksCoveringPip = new HashMap<>();

        addBlock(4, 2, weights, blockList, blocksCoveringPip, X, Y);
        addBlock(2, 4, weights, blockList, blocksCoveringPip, X, Y);
        addBlock(2, 2, weights, blockList, blocksCoveringPip, X, Y);
        addBlock(1, 2, weights, blockList, blocksCoveringPip, X, Y);
        addBlock(2, 1, weights, blockList, blocksCoveringPip, X, Y);

        // final solver attempt

        // Create the linear solver with the GLOP backend.
        Loader.loadNativeLibraries();
        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            System.out.println("Could not create solver GLOP");
            return;
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

            int[][] out = new int[X][Y];
            for (int x = 0; x < X; x++){
                for (int y = 0; y < Y; y++){
                    out[x][y] = -1;
                }
            }

            for (int i = 0; i < blockList.size(); i++){
                if (Math.abs(variables.get(i).solutionValue() - 1.0) > 1e-4){
                    continue;
                }
                block b = blockList.get(i);
                for (Pip p : b.coveredPips()){
                    out[p.x][p.y] = i;
                }
            }

            for (int x = 0; x < X; x++){
                for (int y = 0; y < Y; y++){
                    System.err.print(out[x][y]);
                    System.err.print(" ");
                }
                System.err.println(" ");
            }
            } else {
            System.err.println("The problem does not have an optimal solution!");
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
     */
    private static void addBlock(int X, int Y, float[][] weights, List<block> blockList, Map<Pip, List<Integer>> blocksCoveringPip, 
    int xBound, int yBound){
        for (int x = 0; x < xBound - X + 1; x++){
            for (int y = 0; y < yBound - Y + 1; y++){
                float blockScore = 0;
                List<Pip> coveredPips = new ArrayList<>();

                // loop over all pips to compute the score and save data for ILP
                for (int xx = 0; xx < X; xx++){
                    for (int yy = 0; yy < Y; yy++){
                        blockScore += weights[x + xx][y + yy];
                        Pip pip = new Pip(x + xx, y + yy);
                        coveredPips.add(pip);

                        blocksCoveringPip.computeIfAbsent(pip, k -> new ArrayList<>())
                            .add(blockList.size());
                    }
                }

                blockList.add(new block(coveredPips, blockScore));
            }
        }
    }
}
