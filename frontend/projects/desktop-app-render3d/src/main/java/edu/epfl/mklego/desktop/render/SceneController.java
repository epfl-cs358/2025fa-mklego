package edu.epfl.mklego.desktop.render;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public abstract class SceneController {
    private final BooleanProperty enabledProperty;

    public abstract void control (Scene3D scene);
    public abstract void dispose (Scene3D scene);

    public SceneController () {
        this(false);
    }
    public SceneController (boolean enabled) {
        this.enabledProperty = new SimpleBooleanProperty(enabled);
    }

    public boolean isEnabled  () {
        return enabledProperty.get();
    }
    public void setEnabled (boolean enabled) {
        enabledProperty.set(enabled);
    }

    public BooleanProperty enabledProperty () {
        return enabledProperty;
    }
}
