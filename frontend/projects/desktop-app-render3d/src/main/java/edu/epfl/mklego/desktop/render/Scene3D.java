package edu.epfl.mklego.desktop.render;

import edu.epfl.mklego.desktop.render.camera.CameraController;
import edu.epfl.mklego.desktop.render.mesh.LegoMeshView;
import edu.epfl.mklego.desktop.render.mesh.LegoPieceMesh;
import edu.epfl.mklego.desktop.render.mesh.SimpleMesh;
import edu.epfl.mklego.desktop.utils.Theme;

import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;

public class Scene3D extends SubScene {

    private final CameraController cameraController;

    public CameraController getCameraController () {
        return this.cameraController;
    }

    public Scene3D(Theme theme, double width, double height) {
        super(new Group(), width, height, true, SceneAntialiasing.BALANCED);

        MeshView cubeView = LegoMeshView.makePlate(22, 22, Color.CORNFLOWERBLUE);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        this.cameraController = new CameraController();

        // 5. Create a Group to hold all 3D content
        Group root3D = (Group) this.getRoot();
        AmbientLight ambientLight = new AmbientLight(Color.rgb(100, 100, 100));
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(200);  // 200 units to the right
        pointLight.setTranslateY(300); // 100 units up (Y-axis is typically inverted in JavaFX)
        pointLight.setTranslateZ(300); // 300 units away from the camera (into the scene)
        root3D.getChildren().addAll(cubeView, ambientLight, pointLight);
        
        // 6. Create the SubScene for 3D rendering
        // Antialiasing is set to improve visual quality
        this.setCamera(camera);
        
        // Set a background color for the 3D area
        this.fillProperty().bind(
            theme.makeBinding(
                Color.web("#e9e9e9"), 
                Color.web("#282634ff"))
        );
        
        this.cameraController.control(this);
    }
    
    public void bindSizeToContainer (Region container) {
        this.widthProperty ().bind(container.widthProperty());
        this.heightProperty().bind(container.heightProperty());
    }
}
