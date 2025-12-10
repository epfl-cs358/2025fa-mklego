package edu.epfl.mklego.slicer.voxelizer;

import edu.epfl.mklego.objloader.Mesh;
import edu.epfl.mklego.objloader.Mesh.Triangle;
import javafx.geometry.Point3D;
import java.util.Random;

public class Voxelizer {
    // Convert 3D Mesh into the voxel grid

    // placeholder values
    // mesh coordinates are represented in mm
    private static int verticalDimention = 18;
    // 8 mm x 8 mm x 9.6 mm
    private static float horizontalVoxelSize = 8;
    private static float verticalVoxelHeight = 9.6f;
    private static int numberOfPoints = 100;            // number of points used to approximate each voxel

    static final double EPSILON = 1e-8;                 // can be changed. If too small, intersections may be ignored. If too large
                                                        // false intersections can be recorded

    // precompute uniformly distributed rays on a sphere using Fibonacci sphere sampling                                                    
    private static final Point3D[] UNIFORM_RAYS = fibonacciSphereSampling(60); // can be changed to any number

    // generate n points uniformly distributed on a sphere
    private static Point3D[] fibonacciSphereSampling(int n) {
        Point3D[] dirs = new Point3D[n];

        double yStep = 2.0 / n; // y in [-1, 1] => step = 2/n
        double angleStep = Math.PI * (3.0 - Math.sqrt(5.0)); // golden angle

        for (int i = 0; i < n; i++) {
            double y = ((i * yStep) - 1) + (yStep / 2); // y uniform in [-1, 1]
                                                        // yStep/2 to avoid cluster/singularity at the poles
            double radius = Math.sqrt(1 - y * y);
            double phi = i * angleStep; // each point is rotated by the golden angle relative to the previous one
                                        // => even spacing / uniform disitribution

            double x = Math.cos(phi) * radius;
            double z = Math.sin(phi) * radius;

            dirs[i] = new Point3D(x, y, z);
        }

        return dirs;
    }


    /**
     * Calculates the value for each voxel for whether we want to have a LEGO brick there
     * @param mesh The build mesh that we want to voxelise
     * @return the data for each voxel
     */
    public static float[][][] voxelize (Mesh mesh, int horizontalDimention) {
        // step 1: for each voxel generate some points.
        // setp 2: for each of those points, draw a radius and see with how many points of the mesh it intersects with.
            // if intersections are even, add -1 to the score. Else +1.
            // then normalize the score depending on the number of points to be in [-1, 1]

        float[][][] returnArray = new float[verticalDimention][horizontalDimention][horizontalDimention];
        
        // step 1:
        for (int x = 0; x < horizontalDimention; x++){
            for (int y = 0; y < horizontalDimention; y++){
                for (int z = 0; z < verticalDimention; z++){
                    // step 2:
                    float voxelValue = 0;

                    int n = 5;
                    numberOfPoints = n*n*n;
                    
                    for (int nx = 1; nx < 2*n; nx+=2){
                        for (int ny = 1; ny < 2*n; ny+=2){
                            for (int nz = 1; nz < 2*n; nz+=2){

                                double XPoint = (x + nx / (2.0f*n)) * horizontalVoxelSize;
                                double YPoint = (y + ny / (2.0f*n)) * horizontalVoxelSize;
                                double ZPoint = (z + nz / (2.0f*n)) * verticalVoxelHeight;

                                Point3D origin = new Point3D(XPoint, YPoint, ZPoint);

                                Point3D vector = new Point3D(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1)
                                    .normalize();
            
                                int intersections = getIntersections(mesh, origin, vector);
                                // if number of intersections is odd, we are inside of the structure and add 1 to value
                                voxelValue += intersections % 2 == 1 ? 1 : -1;
                            }
                        }
                    }

                    float score = voxelValue / numberOfPoints;
                    if (score < 0.0f) score *= 2.0f;

                    returnArray[z][x][y] = score;
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
        int numberOfIntersections = 0;
        
        for (Triangle triangle : mesh.triangles()){
            if (MollerTrumbore(origin, vector, triangle)){
                numberOfIntersections++;
            }
        }

        return numberOfIntersections;
    }

    /*
     * use the MollerTrumbore algorithm to calculate whether ray intersects with a triangle
     * code plan taken from wikipedia and then adapted
     */
    private static boolean MollerTrumbore(Point3D rayOrigin, Point3D rayVector, Triangle triangle){

        Point3D vertex0 = triangle.p1();
        Point3D vertex1 = triangle.p2();
        Point3D vertex2 = triangle.p3();
        Point3D edge1 = Point3D.ZERO;
        Point3D edge2 = Point3D.ZERO;
        Point3D h = Point3D.ZERO;
        Point3D s = Point3D.ZERO;
        Point3D q = Point3D.ZERO;
        double a, f, u, v;
        edge1 = vertex1.subtract(vertex0);
        edge2 = vertex2.subtract(vertex0);
        h = rayVector.crossProduct(edge2);
        a = edge1.dotProduct(h);

        if (a > -EPSILON && a < EPSILON) {
            return false;    // This ray is parallel to this triangle.
        }

        f = 1.0 / a;
        s = rayOrigin.subtract(vertex0);
        u = f * (s.dotProduct(h));

        if (u < 0.0 || u > 1.0) {
            return false;
        }

        q = s.crossProduct(edge1);
        v = f * rayVector.dotProduct(q);

        if (v < 0.0 || u + v > 1.0) {
            return false;
        }

        // At this stage we can compute t to find out where the intersection point is on the line.
        double t = f * edge2.dotProduct(q);
        if (t > EPSILON) // ray intersection
        {
            return true;
        }

        // This means that there is a line intersection but not a ray intersection.
        return false;
    }
}