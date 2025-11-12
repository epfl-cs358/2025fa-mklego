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
import edu.epfl.mklego.project.scene.entities.LegoPiece;
import edu.epfl.mklego.project.scene.entities.LegoPiece.StdLegoPieceKind;
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
        if (entity instanceof GroupEntity)
            return render( (GroupEntity) entity );

        throw new RuntimeException("Unknown entity type for rendering: " + entity.getClass().getName());
    }

    public Node render (LegoAssembly assembly, LegoPiece piece, StdLegoPieceKind kind) {
        float deltaXtoStub = piece.getMainStubRow() - (assembly.getPlateNumberRows()    / 2.f);
        float deltaYtoStub = piece.getMainStubCol() - (assembly.getPlateNumberColumns() / 2.f);
        System.out.println(deltaXtoStub + " " + deltaYtoStub);
    
        float deltaXstubToCenter = (kind.getNumberRows()) / 2.f;
        float deltaYstubToCenter = (kind.getNumberColumns()) / 2.f;
        System.out.println(deltaXstubToCenter + " " + deltaYstubToCenter);
    
        float deltaX = deltaXtoStub + deltaXstubToCenter;
        float deltaY = deltaYtoStub + deltaYstubToCenter;
        float deltaZ = piece.getMainStubHeight();

        LegoMeshView view = LegoMeshView.makePiece(
            kind.getNumberColumns(), kind.getNumberRows(), piece.getColor());

        return applyTransformations(
            view,
            new Transform(
                new Observable3f(1, 1, 1), 
                new Observable3f(
                    deltaX * LegoPieceMesh.LEGO_WIDTH,
                    deltaY * LegoPieceMesh.LEGO_WIDTH, 
                    deltaZ * (LegoPieceMesh.STANDARD_HEIGHT * LegoPieceMesh.LEGO_PARAMETER)), 
                new Observable3f(0, 0, 0)));
    }
    public Node render (LegoAssembly assembly, LegoPiece piece) {
        if (piece.getKind() instanceof StdLegoPieceKind)
            return render(assembly, piece, (StdLegoPieceKind) piece.getKind());

        throw new RuntimeException("Unknown entity type for rendering: "
            + piece.getKind().getClass().getName());
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

        for (LegoPiece piece : assembly.getPieces())
            group.getChildren().add(render(assembly, piece));

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
