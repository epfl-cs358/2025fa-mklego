package edu.epfl.mklego.project.scene.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.epfl.mklego.project.scene.Entity;
import edu.epfl.mklego.project.scene.Transform;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class GroupEntity extends Entity {

    private final ListProperty<Entity> entities;

    public List<Entity> getEntities () {
        return Collections.unmodifiableList(entities);
    }
    public ListProperty<Entity> entityProperty () {
        return entities;
    }

    @JsonCreator
    public GroupEntity(
            @JsonProperty("transform") Transform    transform,
            @JsonProperty("name")      String       entityName,
            @JsonProperty("entities")  List<Entity> entities) {
        super(transform, entityName);

        this.entities = new SimpleListProperty<>(
            FXCollections.observableArrayList(new ArrayList<Entity>())
        );

        for (Entity entity : entities) {
            this.entities.add(entity);
        }
    }

}
