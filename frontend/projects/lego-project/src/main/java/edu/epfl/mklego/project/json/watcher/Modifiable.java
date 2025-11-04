package edu.epfl.mklego.project.json.watcher;

import javafx.beans.property.BooleanProperty;

public interface Modifiable {

    public boolean isModified ();
    public BooleanProperty modifiedProperty ();

    public void save ();

}
