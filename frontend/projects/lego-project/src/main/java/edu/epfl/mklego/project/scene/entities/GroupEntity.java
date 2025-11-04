package edu.epfl.mklego.project.scene.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.epfl.mklego.project.json.watcher.SimpleListWatcher;
import edu.epfl.mklego.project.scene.Entity;
import edu.epfl.mklego.project.scene.Transform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class GroupEntity extends Entity {

    private final ListProperty<Entity> entities;
    private final SimpleListWatcher<Entity> watcher;
    private final BooleanProperty isModified;

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

        this.entities = new SimpleListProperty<>( FXCollections.observableArrayList(new ArrayList<Entity>()) );
        for (Entity entity : entities) {
            this.entities.add(entity);
        }

        watcher = new SimpleListWatcher<Entity>( this.entities );
        isModified = watcher.modifiedProperty();
    }
    
    @JsonIgnore
    @Override
    public boolean isModified() {
        return isModified.get();
    }
    @JsonIgnore
    @Override
    public BooleanProperty modifiedProperty() {
        return isModified;
    }
    @Override
    public void save() {
        watcher.save();
    }

}
