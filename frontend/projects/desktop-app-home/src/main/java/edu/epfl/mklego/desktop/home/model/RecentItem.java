package edu.epfl.mklego.desktop.home.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;

import java.time.LocalDateTime;

import edu.epfl.mklego.desktop.render.Scene3D;
import edu.epfl.mklego.project.Project;
import edu.epfl.mklego.desktop.utils.Theme;

public class RecentItem {

    public static final int RENDER_RESOLUTION = 256;

    private final Project project;

    private final StringProperty name;
    private final ObjectProperty<LocalDateTime> lastModified;
    private final ObjectProperty<Image> image;

    public RecentItem (Theme theme, Project project) {
        this.name = new SimpleStringProperty();
        this.lastModified = new SimpleObjectProperty<>();
        this.image = new SimpleObjectProperty<>(null);

        this.project = project;

        this.name.bind(project.nameProperty());
        this.lastModified.bind(project.lastModifiedProperty());

        Scene3D scene = new Scene3D(
            theme,
            project.getScene(), 
            RENDER_RESOLUTION, 
            RENDER_RESOLUTION
        );
        /*
         * This line is really important, do not remove (any node for which
         * we take a snapshot should be connected to a Scene)
         */
        @SuppressWarnings("unused")
        Scene pscene = new Scene(new BorderPane(scene), RENDER_RESOLUTION, RENDER_RESOLUTION);
        this.image.set(scene.snapshot(RENDER_RESOLUTION, RENDER_RESOLUTION));
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

    public Project getProject () {
        return project;
    }
}
