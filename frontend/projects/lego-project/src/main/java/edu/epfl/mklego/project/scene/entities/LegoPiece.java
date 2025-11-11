package edu.epfl.mklego.project.scene.entities;

public class LegoPiece {
    public static abstract class LegoPieceKind {}

    private final int mainStubX;
    private final int mainStubY;

    private final int orientation;
    private final LegoPieceKind kind;
    
    public LegoPiece(int mainStubX, int mainStubY, int orientation, LegoPieceKind kind) {
        this.mainStubX = mainStubX;
        this.mainStubY = mainStubY;
        this.orientation = orientation;
        this.kind = kind;
    }
}
