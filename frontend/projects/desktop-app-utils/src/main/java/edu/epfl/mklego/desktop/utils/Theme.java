package edu.epfl.mklego.desktop.utils;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;

public class Theme {
    private final JMetro metro;

    public void setScene (Scene scene) {
        metro.setScene(scene);
    }
    
    public Style getStyle () {
        return metro.getStyle();
    }
    public void setStyle (Style style) {
        metro.setStyle(style);
    }
    public ObjectProperty<Style> styleProperty () {
        return metro.styleProperty();
    }

    public void useBackground (Node node) {
        node.getStyleClass()
            .add(JMetroStyleClass.BACKGROUND);
    }

    String resourceDetails (Class<?> cls, String styleSheet) {
        if (styleSheet == null) return null;

        try {
            return cls.getResource(styleSheet)
                .toExternalForm();
        } catch (NullPointerException exception) {
            throw new IllegalArgumentException(
                "The style sheet '" + styleSheet + "' does not exist on class " + cls);
        }
    }
    public void useResourceStyles (
            ObservableList<String> styleSheets,
            Class<?> cls,
            String simpleStyleSheet, 
            String lightStyleSheet,
            String darkStyleSheet
        ) {
        
        String resSimple = resourceDetails(cls, simpleStyleSheet);
        String resLight  = resourceDetails(cls, lightStyleSheet);
        String resDark   = resourceDetails(cls, darkStyleSheet);

        if (resSimple != null) {
            styleSheets.add(resSimple);
        }

        String currentRes = switch (metro.getStyle()) {
            case LIGHT -> resLight;
            case DARK -> resDark;
        };
        if (currentRes != null) {
            styleSheets.add(currentRes);
        }

        metro.styleProperty()
            .addListener((obs, old, nwv) -> {
                String oldRes = switch (old) {
                    case LIGHT -> resLight;
                    case DARK -> resDark;
                };
                int index = -1;
                if (oldRes != null) {
                    index = styleSheets.indexOf(oldRes);
                    if (index != -1) {
                        styleSheets.remove(index);
                    }
                }

                String nwvRes = switch (nwv) {
                    case LIGHT -> resLight;
                    case DARK -> resDark;
                };

                if (index != -1) {
                    styleSheets.add(index, nwvRes);
                } else styleSheets.add(nwvRes);
            });
    }

    public Theme (Style style) {
        metro = new JMetro(style);
    }
}
