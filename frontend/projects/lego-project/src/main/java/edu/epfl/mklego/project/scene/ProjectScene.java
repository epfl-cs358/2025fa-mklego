package edu.epfl.mklego.project.scene;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.epfl.mklego.project.json.watcher.Modifiable;
import edu.epfl.mklego.project.scene.entities.GroupEntity;
import javafx.beans.property.BooleanProperty;

public class ProjectScene implements Modifiable {
    
    public static final String SCENE_FILE_LOCATION = "scene.mkl";

    private final GroupEntity rootEntity;

    private final int plateNumberRows;
    private final int plateNumberColumns;

    public int getPlateNumberRows () {
        return plateNumberRows;
    }
    public int getPlateNumberColumns () {
        return plateNumberColumns;
    }
    public GroupEntity getRootEntity () {
        return rootEntity;
    }

    @JsonCreator
    public ProjectScene(
            @JsonProperty("rootEntity")         GroupEntity rootEntity,

            @JsonProperty("plateNumberRows")    int plateNumberRows,
            @JsonProperty("plateNumberColumns") int plateNumberColumns) {
        this.rootEntity = rootEntity;

        this.plateNumberRows = plateNumberRows;
        this.plateNumberColumns = plateNumberColumns;
    }

    public static ProjectScene createEmptyScene (
        String rootName, int plateNumberRows, int plateNumberColumns) {
        GroupEntity root = new GroupEntity(
            new Transform(), rootName, List.of());

        return new ProjectScene(root, plateNumberRows, plateNumberColumns);
    }

    @JsonIgnore
    @Override
    public boolean isModified() {
        return rootEntity.isModified();
    }
    @JsonIgnore
    @Override
    public BooleanProperty modifiedProperty() {
        return rootEntity.modifiedProperty();
    }
    @Override
    public void save() {
        rootEntity.save();
    }

}
