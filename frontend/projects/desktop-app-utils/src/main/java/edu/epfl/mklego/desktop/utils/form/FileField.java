package edu.epfl.mklego.desktop.utils.form;

import java.io.File;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileField extends Button {
    
    private final FileChooser chooser;
    private final Stage       stage;

    private final ObjectProperty<File> currentFileProperty;
    private final StringProperty       noFileTextProperty;

    public FileChooser getFileChooser () {
        return chooser;
    }
    public File getFile () {
        return currentFileProperty.get();
    }
    public void setFile (File file) {
        currentFileProperty.set(file);
    }
    public ObjectProperty<File> fileProperty () {
        return currentFileProperty;
    }
    public String getNoFileText () {
        return noFileTextProperty.get();
    }
    public void setNoFileText (String text) {
        noFileTextProperty.set(text);
    }
    public StringProperty noFileTextProperty () {
        return noFileTextProperty;
    }

    public FileField (Stage stage, String noFileText) {
        this.stage   = stage;
        this.chooser = new FileChooser();

        this.currentFileProperty = new SimpleObjectProperty<File>(null);
        this.noFileTextProperty  = new SimpleStringProperty(noFileText);

        this.textProperty().bind(
            Bindings.createStringBinding(
                () -> {
                    if (currentFileProperty.get() == null)
                        return noFileTextProperty.get();
                    return currentFileProperty.get().getAbsolutePath();
                },
                currentFileProperty,
                noFileTextProperty
            )
        );
    
        this.setOnMouseClicked((event) -> {
            File newFile = this.chooser.showOpenDialog(this.stage);
            this.currentFileProperty.set(newFile);
        });
    }

}
