package edu.epfl.mklego.desktop.alerts;

import javafx.animation.PauseTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public abstract class Alert {
    private Runnable removeAlert = null;

    public abstract Node render ();
    public abstract void start  ();
    
    void setRemove (Runnable removeAlert) {
        this.removeAlert = removeAlert;
    }
    void remove () {
        this.removeAlert.run();
    }

    public Alert delayedRemove (Duration delay) {
        PauseTransition pause = new PauseTransition( delay );
        pause.setOnFinished( evt -> this.remove() );
        pause.play();

        return this;
    }
}
