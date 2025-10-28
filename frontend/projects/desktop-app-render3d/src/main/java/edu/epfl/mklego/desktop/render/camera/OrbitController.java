package edu.epfl.mklego.desktop.render.camera;

import edu.epfl.mklego.desktop.render.Scene3D;
import edu.epfl.mklego.desktop.render.SceneController;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;

public class OrbitController extends SceneController {

    private final Rotate xyPlaneRotation; // = new Rotate(45, Rotate.Z_AXIS);
    private final Rotate yzPlaneRotation; // = new Rotate(-135, Rotate.X_AXIS);

    private double anchorX = 0;
    private double anchorY = 0;
    private double anchorAngleZ = 0;
    private double anchorAngleX = 0;

    private boolean running = false;

    private final Image       orbitIcon;
    private final ImageCursor orbitCursor;
    
    private final DoubleProperty orbitSensitivity = new SimpleDoubleProperty(1.);

    public double getOrbitSensitivity () {
        return orbitSensitivity.get();
    }
    public void setOrbitSensitivity (double value) {
        orbitSensitivity.set(value);
    }
    public DoubleProperty orbitSensitivityProperty () {
        return orbitSensitivity;
    }

    private void updateAnchor (MouseEvent event) {
        anchorAngleZ = xyPlaneRotation.getAngle();
        anchorAngleX = yzPlaneRotation.getAngle();
        anchorX = event.getSceneX();
        anchorY = event.getSceneY();
    }
    
    public Image getOrbitIcon () {
        return orbitIcon;
    }

    public OrbitController (Rotate xyPlaneRotation, Rotate yzPlaneRotation) {
        this.xyPlaneRotation = xyPlaneRotation;
        this.yzPlaneRotation = yzPlaneRotation;

        this.orbitIcon = new Image(
            this.getClass().getResource("orbit_cursor_icon.png").toExternalForm() );
        this.orbitCursor = new ImageCursor(orbitIcon);
    }

    @Override
    public void control(Scene3D scene) {
        scene.setCursor(orbitCursor);

        scene.setOnMousePressed(event -> {
            running = true;
            updateAnchor(event);
        });
        scene.setOnMouseDragged(event -> {
            if (!running) return ;

            yzPlaneRotation.setAngle(
                anchorAngleX + getOrbitSensitivity() * (anchorY - event.getSceneY()));

            double yzAngle = yzPlaneRotation.getAngle() % 360;
            if (yzAngle < 0) yzAngle = 360 + yzAngle;

            double coefBase = 1.;
            if (yzAngle >= 180.) coefBase *= -1;

            double yzAngleLocal = Math.min(
                360. - yzAngle,
                Math.min(yzAngle, Math.abs(180. - yzAngle))
            );
            double coefSwitch = 1.;
            if (yzAngleLocal <= 5) {
                coefSwitch = yzAngleLocal / 5.;
            }

            double coef = coefBase * coefSwitch;
            xyPlaneRotation.setAngle(
                anchorAngleZ
            - coef * getOrbitSensitivity() * (anchorX - event.getSceneX()));

            updateAnchor(event);
        });
        scene.setOnMouseReleased(event -> running = false);
    }

    @Override
    public void dispose(Scene3D scene) {
        scene.setCursor(null);

        scene.setOnMousePressed(null);
        scene.setOnMouseDragged(null);
        
        running = false;
    }
    
}
