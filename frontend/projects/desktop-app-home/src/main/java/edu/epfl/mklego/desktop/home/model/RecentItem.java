package edu.epfl.mklego.desktop.home.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

import java.time.LocalDateTime;
import java.nio.file.Path;

public class RecentItem {

    private final StringProperty name;
    private final ObjectProperty<LocalDateTime> lastModified;
    private final ObjectProperty<Image> image;
    private final Path path;

    public RecentItem(String name, LocalDateTime lastModified, Image image, Path path) {
        this.name = new SimpleStringProperty(name);
        this.lastModified = new SimpleObjectProperty<>(lastModified);
        this.image = new SimpleObjectProperty<>(image);
        this.path = path;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public ObjectProperty<LocalDateTime> lastModifiedProperty() {
        return lastModified;
    }

    public LocalDateTime getLastModified() {
        return lastModified.get();
    }

    public ObjectProperty<Image> imageProperty() {
        return image;
    }

    public Image getImage() {
        return image.get();
    }

    public Path getPath() {
        return path;
    }
}
