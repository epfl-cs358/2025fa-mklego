package edu.epfl.mklego.desktop;

import edu.epfl.mklego.desktop.alerts.AlertPane;
import edu.epfl.mklego.desktop.alerts.AlertQueue;
import edu.epfl.mklego.desktop.alerts.SimpleAlert;
import edu.epfl.mklego.desktop.alerts.SimpleAlert.AlertButton;
import edu.epfl.mklego.desktop.alerts.SimpleAlert.AlertButtonType;
import edu.epfl.mklego.desktop.alerts.SimpleAlert.AlertType;
import edu.epfl.mklego.desktop.alerts.exceptions.AlertAlreadyExistsException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.JMetroStyleClass;
import jfxtras.styles.jmetro.Style;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        Button l = new Button("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        l.setFocusTraversable(false);
        StackPane totalPane = new StackPane(l);
        AlertQueue queue = new AlertQueue();
        JMetro jMetro = new JMetro(Style.DARK);
        AlertPane pane = new AlertPane(queue, jMetro);
        totalPane.getStyleClass().add(JMetroStyleClass.BACKGROUND);
        totalPane.getChildren().add( pane );

        Scene scene = new Scene(totalPane, 640, 480);
        
        try {
            queue.pushBack(
                new SimpleAlert(AlertType.SUCCESS, "Some alert ! Some alert ! Some alert !Some alert ! Some alert ! Some alert !Some alert ! Some alert ! Some alert !")
                    .withSource("Main MKLego")
            );
            queue.pushBack(
                new SimpleAlert(AlertType.INFO, "Some alert !")
                    .withSource("Main MKLego")
            );
            queue.pushBack(
                new SimpleAlert(AlertType.WARNING, "Some alert !")
                    .withSource("Main MKLegoMain MKLegoMain MKLegoMain MKLegoMain MKLegoMain MKLego")
                    .withButton(new AlertButton(AlertButtonType.PRIMARY, "Yes", () -> { System.out.println("WARNING: Yes."); }))
                    .withButton(new AlertButton(AlertButtonType.SECONDARY, "No", () -> { System.out.println("WARNING: No."); }))
                    .withCloseRunnable(() -> { System.out.println("WARNING: Close."); })
            );
            queue.pushBack(
                new SimpleAlert(AlertType.DANGER, "Some alert !")
                    .withSource("Main MKLego")
                    .withButton(new AlertButton(AlertButtonType.PRIMARY, "Yes", () -> { System.out.println("DANGER: Yes."); }))
                    .withButton(new AlertButton(AlertButtonType.SECONDARY, "No", () -> { System.out.println("DANGER: No."); }))
                    .withCloseRunnable(() -> { System.out.println("DANGER: Close."); })
            );
        } catch (AlertAlreadyExistsException e) {
            e.printStackTrace();
        }
        
        stage.setScene(scene);
        jMetro.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}