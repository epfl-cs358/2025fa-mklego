package edu.epfl.mklego.project.scene.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javafx.scene.paint.Color;

public class LegoPiece {
    @JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="kindClass")
    @JsonSubTypes({
        @JsonSubTypes.Type(value = StdLegoPieceKind.class, name = "std")
    })
    public static abstract class LegoPieceKind {}

    public static class StdLegoPieceKind extends LegoPieceKind {
        private final int numberRows;
        private final int numberColumns;

        public int getNumberRows () {
            return numberRows;
        }
        public int getNumberColumns () {
            return numberColumns;
        }
        
        @JsonCreator
        public StdLegoPieceKind (
                @JsonProperty("numberRows")    int numberRows,
                @JsonProperty("numberColumns") int numberColumns) {
            this.numberRows    = numberRows;
            this.numberColumns = numberColumns;
        }
    };

    private final int mainStubRow;
    private final int mainStubCol;
    private final int mainStubHeight;

    private final Color color;

    private final LegoPieceKind kind;

    public int getMainStubRow () {
        return mainStubRow;
    }
    public int getMainStubCol () {
        return mainStubCol;
    }
    public int getMainStubHeight () {
        return mainStubHeight;
    }
    public Color getColor () {
        return color;
    }
    public LegoPieceKind getKind () {
        return kind;
    }

    @JsonCreator
    public LegoPiece(
            @JsonProperty("mainStubRow")    int mainStubRow, 
            @JsonProperty("mainStubCol")    int mainStubCol,
            @JsonProperty("mainStubHeight") int mainStubHeight,

            @JsonProperty("color")     Color color,
            @JsonProperty("kind")      LegoPieceKind kind) {
        this.mainStubRow    = mainStubRow;
        this.mainStubCol    = mainStubCol;
        this.mainStubHeight = mainStubHeight;
        
        this.color = color;
        this.kind  = kind;
    }
}
