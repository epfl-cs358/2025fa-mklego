package edu.epfl.mklego.desktop.render;

import edu.epfl.mklego.desktop.render.mesh.LegoMeshView;
import edu.epfl.mklego.desktop.render.mesh.LegoPieceMesh;
import edu.epfl.mklego.desktop.utils.MappedList;
import edu.epfl.mklego.project.scene.Entity;
import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.Transform;
import edu.epfl.mklego.project.scene.Transform.Observable3f;
import edu.epfl.mklego.project.scene.entities.GroupEntity;
import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import edu.epfl.mklego.project.scene.entities.LegoPieceEntity;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

public class SceneRenderer {
    
    public Node render (Entity entity) {
        if (entity instanceof LegoPieceEntity)
            return render( (LegoPieceEntity) entity );
        if (entity instanceof GroupEntity)
            return render( (GroupEntity) entity );

        throw new RuntimeException("Unknown entity type for rendering: " + entity.getName());
    }

    public Node render (LegoAssembly assembly) {
        Group group = new Group();
        group.getChildren().add(
            applyTransformations(    
                LegoMeshView.makePlate(
                    assembly.getPlateNumberColumns(),
                    assembly.getPlateNumberRows(),
                    Color.CORNFLOWERBLUE),
                new Transform(
                    new Observable3f(1, 1, 1), 
                    new Observable3f(0, 0, - LegoPieceMesh.LEGO_PARAMETER), 
                    new Observable3f(0, 0, 0))
            )
        );

        return group;
    }
    public Node render (ProjectScene scene) {
        Group group = new Group();
        group.getChildren().addAll(
            render(scene.getLegoAssembly()),
            render(scene.getRootEntity())
        );
        
        return group;
    }

    public Node applyTransformations (Node node, Transform transform) {
        Translate translate = new Translate();
        translate.xProperty().bind(transform.getTranslation().xProperty());
        translate.yProperty().bind(transform.getTranslation().yProperty());
        translate.zProperty().bind(transform.getTranslation().zProperty());
        
        Scale scale = new Scale();
        scale.xProperty().bind(transform.getScale().xProperty());
        scale.yProperty().bind(transform.getScale().yProperty());
        scale.zProperty().bind(transform.getScale().zProperty());

        Rotate rotX = new Rotate();
        rotX.setAxis(Rotate.X_AXIS);
        rotX.angleProperty().bind(transform.getRotation().xProperty());
        Rotate rotY = new Rotate();
        rotY.setAxis(Rotate.Y_AXIS);
        rotY.angleProperty().bind(transform.getRotation().yProperty());
        Rotate rotZ = new Rotate();
        rotZ.setAxis(Rotate.Z_AXIS);
        rotZ.angleProperty().bind(transform.getRotation().zProperty());

        node.getTransforms()
            .addAll(
                translate,
                rotZ,
                rotY,
                rotX,
                scale
            );

        return node;
    }

    public Node render (LegoPieceEntity entity) {
        Node node = LegoMeshView.makePiece(
            entity.getNumberColumns(), entity.getNumberRows(), entity.getColor());
        return applyTransformations(node, entity.getTransform());
    }
    public Node render (GroupEntity entity) {
        Group group = new Group();

        ObservableList<Node> nodes = new MappedList<Node, Entity>(
            entity.entityProperty(), ent -> render(ent)
        );
        group.getChildren().addAll(nodes);

        nodes.addListener(new ListChangeListener<Node>() {

            @Override
            public void onChanged(Change<? extends Node> c) {
                group.getChildren().clear();
                group.getChildren().addAll(nodes);
            }
            
        });

        return applyTransformations(group, entity.getTransform());
    }

}
