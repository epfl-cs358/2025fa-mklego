package edu.epfl.mklego.project.json.watcher;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;

public class SimplePropertyWatcher {

    private final List<Modifiable> modifiables = new ArrayList<>();

    private final BooleanProperty innerProperty;

    public SimplePropertyWatcher () {
        innerProperty = new SimpleBooleanProperty();
    }

    public SimplePropertyWatcher withModifiedProperty (BooleanProperty modifiedProperty) {
        modifiedProperty.addListener((obs, old, nwv) -> {
            if (nwv) {
                innerProperty.set(true);
            }
        });
        return this;
    }
    public SimplePropertyWatcher withProperty (BooleanProperty property) {
        property.addListener((obs, old, nwv) -> {
            innerProperty.set(true);
        });
        return this;
    }
    public SimplePropertyWatcher withProperty (FloatProperty property) {
        property.addListener((obs, old, nwv) -> {
            innerProperty.set(true);
        });
        return this;
    }
    public SimplePropertyWatcher withProperty (IntegerProperty property) {
        property.addListener((obs, old, nwv) -> {
            innerProperty.set(true);
        });
        return this;
    }
    public SimplePropertyWatcher withProperty (LongProperty property) {
        property.addListener((obs, old, nwv) -> {
            innerProperty.set(true);
        });
        return this;
    }
    public SimplePropertyWatcher withProperty (DoubleProperty property) {
        property.addListener((obs, old, nwv) -> {
            innerProperty.set(true);
        });
        return this;
    }
    public SimplePropertyWatcher withProperty (StringProperty property) {
        property.addListener((obs, old, nwv) -> {
            innerProperty.set(true);
        });
        return this;
    }
    public SimplePropertyWatcher withModifiable (Modifiable modif) {
        modifiables.add(modif);
        return withProperty(modif.modifiedProperty());
    }

    public BooleanProperty build () {
        return innerProperty;
    }
    public void save () {
        for (Modifiable modif : modifiables)
            modif.save();
    
        innerProperty.set(false);
    }

}
