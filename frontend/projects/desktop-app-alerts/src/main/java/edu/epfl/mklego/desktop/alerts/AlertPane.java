package edu.epfl.mklego.desktop.alerts;

import java.util.HashMap;
import java.util.Map;

import edu.epfl.mklego.desktop.alerts.AlertQueue.AlertQueueListener;
import edu.epfl.mklego.desktop.utils.Theme;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class AlertPane extends BorderPane implements AlertQueueListener {
    public final static String LIGHT_ALERT_CSS = "SimpleAlert-Light.css";
    public final static String DARK_ALERT_CSS  = "SimpleAlert-Dark.css";
    public final static String ALERT_CSS       = "SimpleAlert.css";

    private final AlertQueue queue;
    private final Map<Alert, Node> nodes = new HashMap<>();

    private final VBox container = new VBox();
    private final int  DEFAULT_ALERT_PANE_WIDTH = 300;

    public void setAlertsWidth (double width) {
        container.setPrefWidth(width);
        container.setMinWidth(width);
        container.setMaxWidth(width);
    }
    
    public AlertQueue getQueue () {
        return queue;
    }
    public AlertPane (AlertQueue queue, Theme theme) {
        super();

        setAlertsWidth(DEFAULT_ALERT_PANE_WIDTH);
        setBackground(null);

        Pane centerPane = new Pane();
        
        centerPane.setPickOnBounds(false);
        container.setPickOnBounds(false);
        this.setPickOnBounds(false);

        container.setSpacing(10);
        container.setPadding(new Insets(10));

        Region spacer = new Region();
        spacer.setPickOnBounds(false);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        container.getChildren().add(spacer);

        this.setBackground(Background.EMPTY);
        this.setRight(container);
        this.setCenter(centerPane);

        theme.useResourceStyles(
            getStylesheets(),
            getClass(),
            ALERT_CSS, LIGHT_ALERT_CSS, DARK_ALERT_CSS
        );

        this.queue = queue;
        this.queue.addListener(this);
    }

    @Override
    public void onPushBack(Alert alert) {
        Node rendered = alert.render();
        nodes.put(alert, rendered);
        
        container
            .getChildren()
            .add(1, rendered);
        
        alert.start();
    }
    @Override
    public void onPop(int index, Alert alert) {
        Node rendered = nodes.getOrDefault(alert, null);
        if (rendered == null) return ;

        container
            .getChildren()
            .remove(rendered);
        nodes.remove(alert);
    }
}
