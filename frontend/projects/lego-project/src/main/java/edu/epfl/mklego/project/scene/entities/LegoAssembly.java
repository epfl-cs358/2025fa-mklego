package edu.epfl.mklego.project.scene.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javafx.beans.property.BooleanProperty;

public class LegoAssembly {

    private final int plateNumberRows;
    private final int plateNumberColumns;

    private final List<LegoPiece> pieces;

    public int getPlateNumberRows () {
        return plateNumberRows;
    }
    public int getPlateNumberColumns () {
        return plateNumberColumns;
    }
    public List<LegoPiece> getPieces () {
        return pieces;
    }

    public LegoAssembly(
        @JsonProperty("plateNumberRows")    int plateNumberRows,
        @JsonProperty("plateNumberColumns") int plateNumberColumns,
        @JsonProperty("pieces")             List<LegoPiece> pieces
    ) {
        this.plateNumberRows    = plateNumberRows;
        this.plateNumberColumns = plateNumberColumns;

        this.pieces = new ArrayList<>(pieces);
    }

    @JsonIgnore
    public boolean isModified() {
        return false;
    }

    @JsonIgnore
    public BooleanProperty modifiedProperty() {
        return null;
    }

    public void save() {

    }
    
}
