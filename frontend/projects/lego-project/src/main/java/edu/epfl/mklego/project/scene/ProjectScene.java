package edu.epfl.mklego.project.scene;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.epfl.mklego.project.scene.entities.GroupEntity;

public class ProjectScene {
    
    public static final String SCENE_FILE_LOCATION = "scene.mkl";

    private final GroupEntity rootEntity;

    public GroupEntity getRootEntity () {
        return rootEntity;
    }

    @JsonCreator
    public ProjectScene(
            @JsonProperty("rootEntity") GroupEntity rootEntity) {
        this.rootEntity = rootEntity;
    }

}
