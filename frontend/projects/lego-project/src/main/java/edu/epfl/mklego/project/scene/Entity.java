package edu.epfl.mklego.project.scene;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import edu.epfl.mklego.project.json.watcher.Modifiable;
import edu.epfl.mklego.project.scene.entities.GroupEntity;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="entityType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = GroupEntity.class, name = "group")
})
public abstract class Entity implements Modifiable {

    private final Transform      transform;
    private final StringProperty name;
    
    public Transform getTransform () {
        return transform;
    }
    public String getName () {
        return name.get();
    }

    public Entity (Transform transform, String name) {
        this.transform = transform;
        this.name      = new SimpleStringProperty(name);
    }

}
