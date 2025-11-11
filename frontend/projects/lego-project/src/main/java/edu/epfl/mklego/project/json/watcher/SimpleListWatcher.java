package edu.epfl.mklego.project.json.watcher;

import edu.epfl.mklego.desktop.utils.MappedList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class SimpleListWatcher<U extends Modifiable> implements Modifiable, ListChangeListener<BooleanProperty>, ChangeListener<Boolean> {

    private final BooleanProperty isModified = new SimpleBooleanProperty(false);

    private final ObservableList<U> modifiables;
    
    public SimpleListWatcher (ObservableList<U> modifiables) {
        this.modifiables = modifiables;

        ObservableList<BooleanProperty> properties
                 = new MappedList<>(modifiables, modif -> modif.modifiedProperty());
    
        properties.addListener(this);
        for (BooleanProperty prop : properties) prop.addListener(this);
    }

    @Override
    public boolean isModified() { return isModified.get(); }

    @Override
    public BooleanProperty modifiedProperty() { return isModified; }

    @Override
    public void save() {
        for (Modifiable mod : modifiables) mod.save();

        isModified.set(false);
    }

    @Override
    public void onChanged(Change<? extends BooleanProperty> change) {
        while (change.next()) {
            if (change.wasAdded()) {
                for (BooleanProperty addedProperty : change.getAddedSubList()) {
                    addedProperty.addListener(this);
                }
            }

            if (change.wasRemoved()) {
                for (BooleanProperty removedProperty : change.getRemoved()) {
                    removedProperty.removeListener(this);
                }
            }

            isModified.set(true);
        }
    }

    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (newValue && !oldValue) {
            isModified.set(true);
        }
    }

}
