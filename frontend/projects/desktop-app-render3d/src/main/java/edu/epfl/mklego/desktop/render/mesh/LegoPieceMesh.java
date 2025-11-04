package edu.epfl.mklego.desktop.render.mesh;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.shape.TriangleMesh;

public class LegoPieceMesh extends TriangleMesh {
    public static final float LEGO_PARAMETER = 1.6f;
    public static final float LEGO_WIDTH     = LEGO_PARAMETER * 5.f;
    
    public static final int STANDARD_HEIGHT = 6;
    public static final int PLATE_HEIGHT    = 1;
    
    public static final int    NUM_CIRC_STEPS = 20;
    public static final double CIRCLE_RADIUS  = 1.5f * LEGO_PARAMETER;

    public final int numberRows;
    public final int numberCols;
    
    LegoPieceMesh (int numberRows, int numberCols, int height) {
        if (numberRows <= 0) {
            throw new IllegalArgumentException("A LEGO piece must have a strictly positive number of rows");
        }
        if (numberCols <= 0) {
            throw new IllegalArgumentException("A LEGO piece must have a strictly positive number of columns");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("A LEGO piece must have a strictly positive height");
        }
        getTexCoords().addAll(0.5f, 0.5f);

        this.numberRows = numberRows;
        this.numberCols = numberCols;

        float centerx = numberRows * 5.f * LEGO_PARAMETER / 2.f;
        float centery = numberCols * 5.f * LEGO_PARAMETER / 2.f;

        List<Float> points = new ArrayList<>();

        int[][] points_zp = new int[numberRows + 1][numberCols + 1];
        int[][] points_zn = new int[numberRows + 1][numberCols + 1];

        float pz  = height * LEGO_PARAMETER;
        float pzp = height * LEGO_PARAMETER + LEGO_PARAMETER * (17.f / 16.f);

        int nbPoints = 0;
        for (int x = 0; x <= numberRows; x ++) {
            for (int y = 0; y <= numberCols; y ++) {
                float px = x * 5.f * LEGO_PARAMETER - centerx;
                float py = y * 5.f * LEGO_PARAMETER - centery;

                points_zn[x][y] = nbPoints ++;
                points_zp[x][y] = nbPoints ++;

                points.addAll(List.of(px, py, 0.f, px, py, pz));
            }
        }

        List<Integer> faces = new ArrayList<>();
        for (int x = 0; x < numberRows; x ++) {
            for (int y = 0; y < numberCols; y ++) {
                faces.addAll(
                    List.of(points_zn[x][y], 0, points_zn[x][y + 1], 0, points_zn[x + 1][y], 0));
                faces.addAll(
                    List.of(points_zn[x][y + 1], 0, points_zn[x + 1][y + 1], 0, points_zn[x + 1][y], 0));
                
                float ccx = (x + 0.5f) * LEGO_PARAMETER * 5.f - centerx;
                float ccy = (y + 0.5f) * LEGO_PARAMETER * 5.f - centery;

                int[] points_id_n = new int[NUM_CIRC_STEPS + 1];
                int[] points_id_p = new int[NUM_CIRC_STEPS + 1];
                for (int i = 0; i < NUM_CIRC_STEPS; i ++) {
                    points_id_n[i] = nbPoints ++;
                    points_id_p[i] = nbPoints ++;

                    float time = ((float) i) / ((float) NUM_CIRC_STEPS) * (float) Math.PI * 2.f;

                    points.addAll(
                        List.of(
                            (float) (Math.cos(time) * CIRCLE_RADIUS + ccx),
                            (float) (Math.sin(time) * CIRCLE_RADIUS + ccy),
                            pz));
                    points.addAll(
                        List.of(
                            (float) (Math.cos(time) * CIRCLE_RADIUS + ccx),
                            (float) (Math.sin(time) * CIRCLE_RADIUS + ccy),
                            pzp));
                }
                points_id_n[NUM_CIRC_STEPS] = points_id_n[0];
                points_id_p[NUM_CIRC_STEPS] = points_id_p[0];

                int PL = NUM_CIRC_STEPS >> 2;

                faces.addAll(
                    List.of(points_id_n[3 * PL], 0, points_zp[x][y], 0, points_zp[x + 1][y], 0) );
                faces.addAll(
                    List.of(points_id_n[2 * PL], 0, points_zp[x][y + 1], 0, points_zp[x][y], 0) );
                faces.addAll(
                    List.of(points_id_n[PL], 0, points_zp[x + 1][y + 1], 0, points_zp[x][y + 1], 0) );
                faces.addAll(
                    List.of(points_id_n[0], 0, points_zp[x + 1][y], 0, points_zp[x + 1][y + 1], 0) );

                for (int i = 0; i < PL; i ++) {
                    faces.addAll(
                        List.of(points_id_n[i + 1], 0, points_id_n[i], 0, points_zp[x + 1][y + 1], 0));
                    faces.addAll(
                        List.of(points_id_n[i + 1 + PL], 0, points_id_n[i + PL], 0, points_zp[x][y + 1], 0));
                    faces.addAll(
                        List.of(points_id_n[i + 1 + 2 * PL], 0, points_id_n[i + 2 * PL], 0, points_zp[x][y], 0));
                    faces.addAll(
                        List.of(points_id_n[i + 1 + 3 * PL], 0, points_id_n[i + 3 * PL], 0, points_zp[x +1 ][y], 0));
                }

                for (int i = 0; i < NUM_CIRC_STEPS; i ++) {
                    faces.addAll(
                        List.of(points_id_n[i], 0, points_id_n[i + 1], 0, points_id_p[i], 0) );
                    faces.addAll(
                        List.of(points_id_p[i + 1], 0, points_id_p[i], 0, points_id_n[i + 1], 0) );
                }
                for (int i = 1; i + 1 < NUM_CIRC_STEPS; i ++) {
                    faces.addAll(
                        List.of(points_id_p[0], 0, points_id_p[i], 0, points_id_p[i + 1], 0));
                }
            }
        }
        for (int x = 0; x < numberRows; x ++) {
            faces.addAll(
                List.of(points_zn[x][0], 0, points_zn[x + 1][0], 0, points_zp[x][0], 0));
            faces.addAll(
                List.of(points_zn[x + 1][0], 0, points_zp[x + 1][0], 0, points_zp[x][0], 0));
                
            faces.addAll(
                List.of(
                    points_zn[x + 1][numberCols], 0,
                    points_zn[x][numberCols], 0,
                    points_zp[x][numberCols], 0));
            faces.addAll(
                List.of(
                    points_zp[x + 1][numberCols], 0,
                    points_zn[x + 1][numberCols], 0,
                    points_zp[x][numberCols], 0));
        }
        for (int y = 0; y < numberCols; y ++) {
            faces.addAll(
                List.of(points_zn[0][y + 1], 0, points_zn[0][y], 0, points_zp[0][y], 0));
            faces.addAll(
                List.of(points_zp[0][y + 1], 0, points_zn[0][y + 1], 0, points_zp[0][y], 0));
            
            faces.addAll(
                List.of(
                    points_zn[numberRows][y], 0,
                    points_zn[numberRows][y + 1], 0,
                    points_zp[numberRows][y], 0));
            faces.addAll(
                List.of(
                    points_zn[numberRows][y + 1], 0,
                    points_zp[numberRows][y + 1], 0,
                    points_zp[numberRows][y], 0));
        }
        
        float[] t_points = new float[faces.size() / 2 * 3];

        int nbTPoints = 0;

        int[] t_faces = new int[faces.size()];
        for (int idx = 0; idx < faces.size(); idx += 2) {
            int id = faces.get(idx);
            t_points[nbTPoints] = points.get(3 * id);
            t_points[nbTPoints + 1] = points.get(3 * id + 1);
            t_points[nbTPoints + 2] = points.get(3 * id + 2);
            
            t_faces[idx] = nbTPoints / 3;
            nbTPoints += 3;
            t_faces[idx + 1] = faces.get(idx + 1);
        }

        getPoints().addAll(t_points);
        getFaces().addAll(t_faces);

        /*int numFaces = getFaces().size() / getFaceElementSize(); // Calculate N
        int[] smoothingGroups = new int[numFaces];

        // Assign '1' to every face to make the entire mesh smooth
        for (int i = 0; i < numFaces; i++) {
            smoothingGroups[i] = 1; 
        }

        getFaceSmoothingGroups().addAll(smoothingGroups);*/
    }

    public static LegoPieceMesh createPiece (int numberRows, int numberCols) {
        return new LegoPieceMesh(numberRows, numberCols, STANDARD_HEIGHT);
    }
    public static LegoPieceMesh createPlate (int numberRows, int numberCols) {
        return new LegoPieceMesh(numberRows, numberCols, PLATE_HEIGHT);
    }
}
