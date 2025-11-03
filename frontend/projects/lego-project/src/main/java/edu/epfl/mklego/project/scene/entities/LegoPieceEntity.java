package edu.epfl.mklego.project.scene.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.epfl.mklego.project.scene.Entity;
import edu.epfl.mklego.project.scene.Transform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.paint.Color;

public class LegoPieceEntity extends Entity {

    private final int numberRows;
    private final int numberColumns;

    private final Color color;

    public int getNumberRows () {
        return numberRows;
    }
    public int getNumberColumns () {
        return numberColumns;
    }

    public Color getColor () {
        return color;
    }

    @JsonCreator
    public LegoPieceEntity(
            @JsonProperty("transform")     Transform transform,
            @JsonProperty("name")          String entityName,
            @JsonProperty("color")         Color color,
            @JsonProperty("numberRows")    int numberRows, 
            @JsonProperty("numberColumns") int numberColumns) {
        super(transform, entityName);

        this.color = color;

        this.numberRows    = numberRows;
        this.numberColumns = numberColumns;
    }
    
    @JsonIgnore
    @Override
    public boolean isModified() {
        return getTransform().isModified();
    }
    @JsonIgnore
    @Override
    public BooleanProperty modifiedProperty() {
        return getTransform().modifiedProperty();
    }
    @Override
    public void save() {
        getTransform().save();
    }
    
}
