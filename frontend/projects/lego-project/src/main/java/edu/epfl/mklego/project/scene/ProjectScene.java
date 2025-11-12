package edu.epfl.mklego.project.scene;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.epfl.mklego.project.json.watcher.Modifiable;
import edu.epfl.mklego.project.scene.entities.GroupEntity;
import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import javafx.beans.property.BooleanProperty;

public class ProjectScene implements Modifiable {
    
    public static final String SCENE_FILE_LOCATION = "scene.mkl";

    private final GroupEntity rootEntity;

    private final LegoAssembly assembly;

    public LegoAssembly getLegoAssembly () {
        return assembly;
    }
    public GroupEntity getRootEntity () {
        return rootEntity;
    }

    @JsonCreator
    public ProjectScene(
            @JsonProperty("rootEntity")   GroupEntity rootEntity,
            @JsonProperty("legoAssembly") LegoAssembly assembly) {
        this.rootEntity = rootEntity;

        this.assembly = assembly;
    }

    public static ProjectScene createEmptyScene (
        String rootName, int plateNumberRows, int plateNumberColumns) {
        GroupEntity root = new GroupEntity(
            new Transform(), rootName, List.of());

        return new ProjectScene(root, new LegoAssembly(plateNumberRows, plateNumberColumns, List.of()));
    }
    public static ProjectScene createSceneFrom (
        String rootName, LegoAssembly assembly) {
        GroupEntity root = new GroupEntity(
            new Transform(), rootName, List.of());

        return new ProjectScene(root, assembly);
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
