package edu.epfl.mklego.desktop.render.camera;

import edu.epfl.mklego.desktop.render.Scene3D;
import edu.epfl.mklego.desktop.render.SceneController;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class PanController extends SceneController {
    
    private final Rotate xyPlaneRotation;
    private final Rotate yzPlaneRotation;

    private final Translate pTranslation;

    private double anchorX = 0;
    private double anchorY = 0;

    private boolean running = false;

    private final DoubleProperty panSensitivity = new SimpleDoubleProperty(1.);

    public double getPanSensitivity () {
        return panSensitivity.get();
    }
    public void setPanSensitivity (double value) {
        panSensitivity.set(value);
    }
    public DoubleProperty panSensitivityProperty () {
        return panSensitivity;
    }

    private void updateAnchor (MouseEvent event) {
        anchorX = event.getSceneX();
        anchorY = event.getSceneY();
    }
    public PanController (Rotate xyPlaneRotation, Rotate yzPlaneRotation, Translate pTranslation) {
        this.xyPlaneRotation = xyPlaneRotation;
        this.yzPlaneRotation = yzPlaneRotation;
    
        this.pTranslation = pTranslation;
    }

    @Override
    public void control(Scene3D scene) {
        scene.setCursor(Cursor.OPEN_HAND);

        scene.setOnMousePressed(event -> {
            scene.setCursor(Cursor.CLOSED_HAND);

            running = true;
            updateAnchor(event);
        });
        scene.setOnMouseDragged(event -> {
            if (!running) return ;

            double dx = event.getSceneX() - anchorX;
            double dy = event.getSceneY() - anchorY;
            
            Point3D point3d = new Point3D(- dx, - dy, 0);
            Rotate invertXYRotate = new Rotate(xyPlaneRotation.getAngle(), Rotate.Z_AXIS);
            Rotate invertYZRotate = new Rotate(yzPlaneRotation.getAngle(), Rotate.X_AXIS);

            point3d = invertYZRotate.transform(point3d);
            point3d = invertXYRotate.transform(point3d);

            pTranslation.setX(pTranslation.getX() + point3d.getX());
            pTranslation.setY(pTranslation.getY() + point3d.getY());
            pTranslation.setZ(pTranslation.getZ() + point3d.getZ());

            updateAnchor(event);
        });
        scene.setOnMouseReleased(event -> {
            scene.setCursor(Cursor.OPEN_HAND);
            running = false;
        });
    }
    @Override
    public void dispose(Scene3D scene) {
        scene.setCursor(null);

        scene.setOnMousePressed(null);
        scene.setOnMouseDragged(null);

        running = false;
    }

}
