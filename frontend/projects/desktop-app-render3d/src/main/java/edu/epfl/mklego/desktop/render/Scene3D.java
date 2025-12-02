package edu.epfl.mklego.desktop.render;

import edu.epfl.mklego.desktop.render.camera.CameraController;
import edu.epfl.mklego.desktop.render.mesh.LegoPieceMesh;
import edu.epfl.mklego.desktop.utils.Theme;
import edu.epfl.mklego.project.scene.ProjectScene;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class Scene3D extends SubScene {

    private final CameraController cameraController;
    private final ProjectScene projectScene;

    public CameraController getCameraController () {
        return this.cameraController;
    }
    public ProjectScene getProjectScene() {
        return this.projectScene;
    }

    public Scene3D(Theme theme, ProjectScene scene, double width, double height) {
        super(new Group(), width, height, true, SceneAntialiasing.BALANCED);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(10000.0);

        this.cameraController = new CameraController();
        this.projectScene = scene;

        Node sceneNode = new SceneRenderer().render(scene);

        // 5. Create a Group to hold all 3D content
        Group root3D = (Group) this.getRoot();
        AmbientLight ambientLight = new AmbientLight(Color.rgb(100, 100, 100));
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(200);  // 200 units to the right
        pointLight.setTranslateY(300); // 100 units up (Y-axis is typically inverted in JavaFX)
        pointLight.setTranslateZ(300); // 300 units away from the camera (into the scene)
        root3D.getChildren().addAll(sceneNode, ambientLight, pointLight);
        
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

    public Image snapshot (int width, int height) {
        WritableImage image = new WritableImage(width, height);
        setWidth  (width);
        setHeight (height);
        this.snapshot(null, image);

        return image;
    }

    // Highlighting support for selected meshes
    public void highlightMesh(LegoPieceMesh mesh, boolean highlight) {
        // TODO: implement highlighting (e.g., change material or add outline)
    }
}
