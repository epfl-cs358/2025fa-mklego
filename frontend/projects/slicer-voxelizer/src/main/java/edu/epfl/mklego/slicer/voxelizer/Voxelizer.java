package edu.epfl.mklego.slicer.voxelizer;

import edu.epfl.mklego.objloader.Mesh;
import javafx.geometry.Point3D;
import java.util.Random;

public class Voxelizer {
    // Convert 3D Mesh into the voxel grid

    // placeholder values
    private static int verticalHeight = 22;
    private static int horizontalVoxelSize = 100;
    private static int verticalVoxelHeight = 100;
    private static int rayLength = horizontalVoxelSize * 22;      // max range you would need to go from one end to the other end of the field
    private static int numberOfPoints = 100;            // number of points used to approximate each voxel

    /**
     * Calculates the value for each voxel for whether we want to have a LEGO brick there
     * @param mesh The build mesh that we want to voxelise
     * @return the data for each voxel
     */
    public static float[][][] voxelize (Mesh mesh) {
        // step 1: for each voxel generate some points.
        // setp 2: for each of those points, draw a radius and see with how many points of the mesh it intersects with.
            // if intersections are even, add -1 to the score. Else +1.
            // then normalize the score depending on the number of points to be in [-1, 1]

                
        // Creating the instance of Random class
        Random r = new Random();

        float[][][] returnArray = new float[22][22][verticalHeight];
        
        // step 1:
        for (int x = 0; x < 22; x++){
            for (int y = 0; y < 22; y++){
                for (int z = 0; z < verticalHeight; z++){
                    // step 2:
                    float voxelValue = 0;
                    for (int i = 0; i < numberOfPoints; i++){
                        int XPoint = r.nextInt(horizontalVoxelSize) + x * horizontalVoxelSize;
                        int YPoint = r.nextInt(horizontalVoxelSize) + y * horizontalVoxelSize;
                        int ZPoint = r.nextInt(verticalVoxelHeight) + z * verticalVoxelHeight;
                        Point3D origin = new Point3D(XPoint, YPoint, ZPoint);
                        Point3D vector = new Point3D(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1)
                            .normalize();

                        int intersections = getIntersections(mesh, origin, vector);
                        // if number of intersections is odd, we are inside of the structure and add 1 to value
                        voxelValue += intersections % 2 == 1 ? 1 : -1;
                    }
                    returnArray[x][y][z] = voxelValue / numberOfPoints;
                }
            }
        }
        return returnArray;
    }

    /**
     * Calculate the number of intersections with the mesh
     * @param mesh the mesh, or some other data structure that makes it easyer to query
     * @param origin the point where the ray is to be shot from
     * @param vector the vector that we want to get the intersections of
     */
    private static int getIntersections(Mesh mesh, Point3D origin, Point3D vector){
        return 0;
    }
}