package edu.epfl.mklego.slicer.voxelizer;

import edu.epfl.mklego.objloader.Mesh;
import edu.epfl.mklego.objloader.Mesh.Triangle;
import javafx.geometry.Point3D;
import java.util.Random;

public class Voxelizer {
    // Convert 3D Mesh into the voxel grid

    // placeholder values
    // mesh coordinates are represented in mm
    private static int verticalHeight = 18;
    // 8 mm x 8 mm x 9.6 mm
    private static float horizontalVoxelSize = 8;
    private static float verticalVoxelHeight = 9.6f;
    private static float rayLength = horizontalVoxelSize * 22;      // max range you would need to go from one end to the other end of the field
    private static int numberOfPoints = 100;            // number of points used to approximate each voxel

    static final double EPSILON = 1e-8;                 // can be changed. If too small, intersections may be ignored. If too large
                                                        // false intersections can be recorded

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
                        float XPoint = r.nextFloat(horizontalVoxelSize) + x * horizontalVoxelSize;
                        float YPoint = r.nextFloat(horizontalVoxelSize) + y * horizontalVoxelSize;
                        float ZPoint = r.nextFloat(verticalVoxelHeight) + z * verticalVoxelHeight;
                        Point3D origin = new Point3D(XPoint, YPoint, ZPoint);
                        Point3D vector = new Point3D(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1)
                            .normalize();

                        int intersections = getIntersections(mesh, origin, vector);
                        // if number of intersections is odd, we are inside of the structure and add 1 to value
                        voxelValue += intersections % 2 == 1 ? 1 : -1;
                    }
                    returnArray[z][x][y] = voxelValue / numberOfPoints;
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