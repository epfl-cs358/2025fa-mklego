package edu.epfl.mklego.desktop.render.camera;

import edu.epfl.mklego.desktop.render.Scene3D;
import edu.epfl.mklego.desktop.render.SceneController;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.transform.Translate;

public class ScrollController extends SceneController {

    private final Translate      zTranslation;
    private final DoubleProperty scrollSensitivity;

    public Translate getZTranslation(){
        return this.zTranslation;
    }
    public double getScrollSensitivity () {
        return scrollSensitivity.get();
    }
    public void setScrollSensitivity (double value) {
        scrollSensitivity.set(value);
    }
    public DoubleProperty scrollSensitivityProperty () {
        return scrollSensitivity;
    }

    public ScrollController (Translate zTranslation) {
        this.zTranslation      = zTranslation;
        this.scrollSensitivity = new SimpleDoubleProperty(1.);
    }

    @Override
    public void control(Scene3D scene) {
        scene.setOnScroll(event -> {
            double dy = event.getDeltaY();
            
            if (dy < 0) zTranslation.setZ(zTranslation.getZ() * (1. + scrollSensitivity.get()));
            else if (dy > 0) zTranslation.setZ(zTranslation.getZ() / (1. + scrollSensitivity.get()));
        });
    }

    @Override
    public void dispose(Scene3D scene) {
        scene.setOnScroll(null);
    }
    
}
