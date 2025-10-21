package edu.epfl.mklego.desktop.menubar;

import edu.epfl.mklego.desktop.alerts.AlertQueue;
import edu.epfl.mklego.desktop.alerts.SimpleAlert;
import edu.epfl.mklego.desktop.alerts.SimpleAlert.AlertType;
import edu.epfl.mklego.desktop.alerts.exceptions.AlertAlreadyExistsException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class BorderlessController {
    private double xOffset = 0;
    private double yOffset = 0;
    
    private double xStage = 0;
    private double yStage = 0;

    private double xScreen = 0;
    private double yScreen = 0;

    private double width  = 0;
    private double height = 0;

    private BooleanProperty isMaximizedProperty = new SimpleBooleanProperty(false);

    private final Stage stage;

    public BooleanProperty isMaximizedProperty () {
        return this.isMaximizedProperty;
    }
    public boolean isMaximized () {
        return this.isMaximizedProperty.get();
    }
    public void setIsMaximized (boolean isMaximized) {
        this.isMaximizedProperty.set(isMaximized);
    }

    public BorderlessController (Stage primaryStage, AlertQueue queue) {
        this.stage = primaryStage;

        this.isMaximizedProperty.addListener((obs, old, nwv) -> {
            if (old && !nwv) {
                stage.setX(xStage);
                stage.setY(yStage);

                stage.setWidth (width);
                stage.setHeight(height);
            } else if (!old && nwv) {
                xStage = stage.getX();
                yStage = stage.getY();

                width  = stage.getWidth();
                height = stage.getHeight();

                ObservableList<Screen> screens = Screen.getScreensForRectangle(
                    stage.getX(), 
                    stage.getY(), 
                    stage.getWidth(), 
                    stage.getHeight()
                );
                if (screens.size() == 0) {
                    try {
                        queue.pushBack(
                            new SimpleAlert(
                                AlertType.DANGER, 
                                "Could not find screen size to maximize window."
                            ).withSource("Borderless Controller") );
                    } catch (AlertAlreadyExistsException e) {}
                    setIsMaximized(false);
                    return ;
                }

                Screen screen = screens.get(0);
                Rectangle2D visualBounds = screen.getVisualBounds();
                double x = visualBounds.getMinX();
                double y = visualBounds.getMinY();
                double width = visualBounds.getWidth();
                double height = visualBounds.getHeight();

                stage.setX(x);
                stage.setY(y);
                stage.setWidth(width);
                stage.setHeight(height);
            }
        });
    }

    void recomputeParameters (MouseEvent event) {
        xOffset = event.getScreenX() - stage.getX();
        yOffset = event.getScreenY() - stage.getY();

        xScreen = event.getScreenX();
        yScreen = event.getScreenY();

        width  = stage.getWidth();
        height = stage.getHeight();
    }

    public void registerTopBar (Node topBar) {
        topBar.setOnMousePressed(event -> {
            if (isMaximized()) return ;

            recomputeParameters(event);
            event.consume();
        });
        topBar.setOnMouseDragged(event -> {
            if (isMaximized()) return ;
            
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
            
            recomputeParameters(event);
            event.consume();
        });
    }

    public static enum HorizontalResize { LEFT, RIGHT, NONE; }
    public static enum VerticalResize   { TOP, BOTTOM, NONE; }
    public void registerResizeGroup (Node group, HorizontalResize hori, VerticalResize vert) {
        group.setOnMousePressed(event -> {
            if (isMaximized()) return ;

            recomputeParameters(event);
            event.consume();
        });
        group.setOnMouseDragged(event -> {
            if (isMaximized()) return ;

            double sx = event.getScreenX() - xOffset;
            double sy = event.getScreenY() - yOffset;
            double dx = event.getScreenX() - xScreen;
            double dy = event.getScreenY() - yScreen;

            switch (hori) {
                case LEFT:
                    stage.setX(sx);
                    stage.setWidth(width - dx);
                    break ;
                case RIGHT:
                    stage.setWidth(width + dx);
                    break ;
                default: break ;
            }
            switch (vert) {
                case TOP:
                    stage.setY(sy);
                    stage.setHeight(height - dy);
                    break ;
                case BOTTOM:
                    stage.setHeight(height + dy);
                    break ;
                default: break ;
            }

            recomputeParameters(event);
        });
    }
}
