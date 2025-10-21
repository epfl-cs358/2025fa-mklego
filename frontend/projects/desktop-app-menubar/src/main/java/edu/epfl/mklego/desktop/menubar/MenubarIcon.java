package edu.epfl.mklego.desktop.menubar;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class MenubarIcon {
    private final Property<Image> iconProperty = new SimpleObjectProperty<>(null);

    public Image getIcon () {
        return iconProperty.getValue();
    }
    public void setIcon (Image icon) {
        iconProperty.setValue(icon);
    }
    public Property<Image> getIconProperty () {
        return iconProperty;
    }

    public Node render () {
        ImageView view = new ImageView();
        view.imageProperty().bind( iconProperty );

        view.setFitWidth (20);
        view.setFitHeight(20);
        
        return view;
    }
}
