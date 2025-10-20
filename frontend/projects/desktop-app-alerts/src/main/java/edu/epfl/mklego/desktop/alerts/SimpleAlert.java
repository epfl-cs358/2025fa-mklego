package edu.epfl.mklego.desktop.alerts;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.MDL2IconFont;

public class SimpleAlert extends Alert {

    private final int DEFAULT_ICON_SIZE = 11;
    
    public static enum AlertType {
        SUCCESS ("\uE73E", "success"),
        INFO    ("\uE946", "info"),
        WARNING ("\uE814", "warning"),
        DANGER  ("\uE783", "danger");

        public final String icon, cls;

        AlertType (String icon, String cls) {
            this.icon = icon;
            this.cls  = cls;
        }
    };

    public static enum AlertButtonType {
        PRIMARY, SECONDARY;
    };
    public static record AlertButton(AlertButtonType type, String text, Runnable action) {
        Region render (Alert alert) {
            Button btn = new Button(text);
            btn.setOnMouseClicked(evt -> {
                alert.remove();

                action.run();
            });

            if (type == AlertButtonType.PRIMARY) {
                btn.getStyleClass().add("button-default");
            }

            btn.setFocusTraversable(false);

            return btn;
        }
    };

    private final List<AlertButton> alertButtons = new ArrayList<>();
    private final String text;
    private final AlertType alertType;

    private String   source = null;
    private Duration displayDuration = null;
    private Runnable closeRunnable   = null;

    public SimpleAlert (AlertType alert, String text) {
        this.text = text;
        this.alertType = alert;
    }

    public SimpleAlert withSource (String source) {
        this.source = source;

        return this;
    }
    public SimpleAlert withButton (AlertButton button) {
        alertButtons.add(button);

        return this;
    }
    public SimpleAlert withDisplayDuration (Duration duration) {
        this.displayDuration = duration;

        return this;
    }
    public SimpleAlert withCloseRunnable (Runnable runnable) {
        this.closeRunnable = runnable;

        return this;
    }

    Node renderBottom () {
        if (alertButtons.size() == 0 && this.source == null)
            return null;

        HBox box = new HBox();
        if (this.source != null) {
            Label sourceLabel = new Label("Source: " + source);
            sourceLabel.setWrapText(false);
            VBox sourceVBox = new VBox(sourceLabel);
            sourceVBox.setAlignment(Pos.CENTER_LEFT);
            box.getChildren().add(sourceVBox);
        }

        HBox rightBox = new HBox();
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        Region space = new Region();
        HBox.setHgrow(space, Priority.ALWAYS);
        rightBox.getChildren().add(space);
        rightBox.setMinWidth(Region.USE_COMPUTED_SIZE);

        boolean isFirstButton = true;
        for (AlertButton button : alertButtons) {
            Region rendered = button.render(this);
            HBox.setHgrow(space, Priority.SOMETIMES);
            rendered.setMinWidth(Region.USE_PREF_SIZE);
            rightBox.getChildren().add(rendered);
            if (!isFirstButton) {
                HBox.setMargin(
                    rendered, new Insets(0, 0, 0, 5));
            }
            isFirstButton = false;
        }

        box.getChildren().add(rightBox);

        box.setPadding(new Insets(5, 0, 0, 0));
        return box;
    }
    @Override
    public Node render() {
        BorderPane result = new BorderPane();
        Label textLabel = new Label(text != null ? text : "<Missing alert text>");
        textLabel.setWrapText(true);
        result.setCenter(new HBox(textLabel));
        result.setPadding(new Insets(5));
        result.getStyleClass().add("simple-alert-box");

        MDL2IconFont typeIcon = new MDL2IconFont(alertType.icon);
        typeIcon.setSize(DEFAULT_ICON_SIZE);
        typeIcon.getStyleClass().add(alertType.cls);
        VBox typeIconBox = new VBox(typeIcon);
        result.setLeft(typeIconBox);
        BorderPane.setMargin(typeIconBox, new Insets(2, 5, 0, 2));
        
        MDL2IconFont closeIcon = new MDL2IconFont("\uF13D");
        closeIcon.setSize(DEFAULT_ICON_SIZE);
        VBox closeIconBox = new VBox(closeIcon);
        closeIcon.getStyleClass().add("close-icon");
        result.setRight(closeIconBox);
        closeIcon.setOnMouseClicked(evt -> {
            if (closeRunnable != null) {
                closeRunnable.run();
            }
            remove();
        });
        BorderPane.setMargin(closeIconBox, new Insets(2, 5, 0, 2));
        
        Node bottom = renderBottom();
        if (bottom != null)
            result.setBottom(bottom);

        result
            .getStyleClass()
            .add(JMetroStyleClass.BACKGROUND);

        return result;
    }
    @Override
    public void start () {
        if (displayDuration == null) return ;

        this.delayedRemove(displayDuration);
    }
    
}
