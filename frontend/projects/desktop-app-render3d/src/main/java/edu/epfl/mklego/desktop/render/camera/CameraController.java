package edu.epfl.mklego.desktop.render.camera;

import edu.epfl.mklego.desktop.render.Scene3D;
import edu.epfl.mklego.desktop.render.SceneController;

import javafx.scene.Camera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class CameraController extends SceneController {

    private Rotate xyPlaneRotation = new Rotate(45, Rotate.Z_AXIS);
    private Rotate yzPlaneRotation = new Rotate(-135, Rotate.X_AXIS);

    private Translate zTranslation = new Translate(0, 0, -500);
    private Translate pTranslation = new Translate(0, 0, 0);

    private final ScrollController scrollController = new ScrollController(zTranslation);
    private final OrbitController  orbitController  = new OrbitController(xyPlaneRotation, yzPlaneRotation);
    private final PanController    panController    = new PanController(xyPlaneRotation, yzPlaneRotation, pTranslation);

    public ScrollController getScrollController () {
        return scrollController;
    }
    public OrbitController getOrbitController () {
        return orbitController;
    }
    public PanController getPanController () {
        return panController;
    }

    @Override
    public void control(Scene3D scene) {
        Camera camera = scene.getCamera();

        camera.getTransforms()
            .addAll(
                pTranslation,
                xyPlaneRotation, 
                yzPlaneRotation,
                zTranslation);
        
        scrollController.control(scene);

        orbitController.enabledProperty().addListener(
            (obs, old, nwv) -> {
                if (old != nwv) {
                    if (nwv) {
                        orbitController.control(scene);
                    } else {
                        orbitController.dispose(scene);
                    }
                }
            });
        panController.enabledProperty().addListener(
            (obs, old, nwv) -> {
                if (old != nwv) {
                    if (nwv) {
                        panController.control(scene);
                    } else {
                        panController.dispose(scene);
                    }
                }
            }
        );
    }

    @Override
    public void dispose(Scene3D scene) {
        scrollController.dispose(scene);
    }
    
}
