package edu.epfl.mklego.desktop.render;

import edu.epfl.mklego.desktop.render.mesh.LegoMeshView;
import edu.epfl.mklego.project.scene.Entity;
import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.entities.GroupEntity;
import edu.epfl.mklego.project.scene.entities.LegoPieceEntity;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;

public class SceneRenderer {
    
    public Node render (Entity entity) {
        if (entity instanceof LegoPieceEntity)
            return render( (LegoPieceEntity) entity );
        if (entity instanceof GroupEntity)
            return render( (GroupEntity) entity );

        throw new RuntimeException("Unknown entity type for rendering: " + entity.getName());
    }

    public Node render (ProjectScene scene) {
        Group group = new Group();
        group.getChildren().add(LegoMeshView.makePlate(
            scene.getPlateNumberColumns(), scene.getPlateNumberRows(), Color.CORNFLOWERBLUE));
        
        return group;
    }

    public Node render (LegoPieceEntity entity) {
        return null;
    }
    public Node render (GroupEntity entity) { return null; }

}
