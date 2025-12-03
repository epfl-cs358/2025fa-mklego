package edu.epfl.mklego.desktop.render.mesh;

import edu.epfl.mklego.project.scene.entities.LegoPiece;
import javafx.scene.paint.Color;

public class LegoMeshView extends SimpleMesh {

    private LegoPiece modelPiece;

    public void setModelPiece(LegoPiece p) {
        this.modelPiece = p;
    }

    public LegoPiece getModelPiece() {
        return modelPiece;
    }

    
    LegoMeshView (LegoPieceMesh mesh) {
        super(mesh);
    }
    LegoMeshView (LegoPieceMesh mesh, Color color) {
        super(mesh, color);
    }

    private int currentXPos = 0;
    private int currentYPos = 0;

    public void setPosition (int xPos, int yPos) {
        setTranslateX((xPos - currentXPos) * LegoPieceMesh.LEGO_WIDTH);
        setTranslateY((yPos - currentYPos) * LegoPieceMesh.LEGO_WIDTH);

        currentXPos = xPos;
        currentYPos = yPos;
    }

    public static LegoMeshView makePlate (int nbCols, int nbRows) {
        return new LegoMeshView(LegoPieceMesh.createPlate(nbRows, nbCols));
    }
    public static LegoMeshView makePlate (int nbCols, int nbRows, Color color) {
        return new LegoMeshView(LegoPieceMesh.createPlate(nbRows, nbCols), color);
    }
    
    public static LegoMeshView makePiece (int nbCols, int nbRows) {
        return new LegoMeshView(LegoPieceMesh.createPiece(nbRows, nbCols));
    }
    public static LegoMeshView makePiece (int nbCols, int nbRows, Color color) {
        return new LegoMeshView(LegoPieceMesh.createPiece(nbRows, nbCols), color);
    }
}
