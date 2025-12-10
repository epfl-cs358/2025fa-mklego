package edu.epfl.mklego.desktop;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import edu.epfl.mklego.desktop.alerts.AlertPane;
import edu.epfl.mklego.desktop.alerts.AlertQueue;
import edu.epfl.mklego.desktop.alerts.SimpleAlert;
import edu.epfl.mklego.desktop.alerts.SimpleAlert.AlertType;
import edu.epfl.mklego.desktop.alerts.exceptions.AlertAlreadyExistsException;
import edu.epfl.mklego.desktop.home.ImportProjectForm;
import edu.epfl.mklego.desktop.home.RecentGrid;
import edu.epfl.mklego.desktop.home.model.RecentItem;
import edu.epfl.mklego.desktop.menubar.BorderlessScene;
import edu.epfl.mklego.desktop.menubar.MenubarIcon;
import edu.epfl.mklego.desktop.render.EditingController;
import edu.epfl.mklego.desktop.render.Scene3D;
import edu.epfl.mklego.desktop.utils.MappedList;
import edu.epfl.mklego.desktop.utils.Theme;
import edu.epfl.mklego.desktop.utils.form.ModalFormContainer;
import edu.epfl.mklego.lgcode.LGCode;
import edu.epfl.mklego.lgcode.ProjectConverter;
import edu.epfl.mklego.project.ProjectException;
import edu.epfl.mklego.project.ProjectManager;
import edu.epfl.mklego.project.scene.entities.LegoPiece.StdLegoPieceKind;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import jfxtras.styles.jmetro.Style;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

import edu.epfl.mklego.slicer.Slicer;

public class Main extends Application {

    private static CheckMenuItem createMenuItem (String title){
        CheckMenuItem cmi = new CheckMenuItem(title);
        cmi.setSelected(true);
        return cmi;
    }

    @SuppressWarnings("unchecked")
    private final Entry<String, Effect>[] effects = new Entry[] {
        new SimpleEntry<String, Effect>("Sepia Tone", new SepiaTone()),
        new SimpleEntry<String, Effect>("Glow", new Glow()),
        new SimpleEntry<String, Effect>("Shadow", new DropShadow())
    };

    public MenuBar exampleMenuBar (Node img) {
        MenuBar menuBar = new MenuBar();

        // --- Menu File
        Menu menuFile = new Menu("File");
        MenuItem add = new MenuItem("Shuffle", img);
        MenuItem clear = new MenuItem("Clear");
        clear.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction((ActionEvent t) -> System.exit(0));
        menuFile.getItems().addAll(add, clear, new SeparatorMenuItem(), exit);

        // --- Menu Edit
        Menu menuEdit = new Menu("Edit");
        Menu menuEffect = new Menu("Picture Effect");

        final ToggleGroup groupEffect = new ToggleGroup();
        for (Entry<String, Effect> effect : effects) {
            RadioMenuItem itemEffect = new RadioMenuItem(effect.getKey());
            itemEffect.setUserData(effect.getValue());
            itemEffect.setToggleGroup(groupEffect);
            menuEffect.getItems().add(itemEffect);
        }

        final MenuItem noEffects = new MenuItem("No Effects");
        noEffects.setDisable(true);

        menuEdit.getItems().addAll(menuEffect, noEffects);

        // --- Menu View
        Menu menuView = new Menu("View");
        CheckMenuItem titleView = createMenuItem("Title");
        CheckMenuItem binNameView = createMenuItem("Binomial name");
        CheckMenuItem picView = createMenuItem("Picture");
        CheckMenuItem descriptionView = createMenuItem("Decsription");
        menuView.getItems().addAll(titleView, binNameView, picView, descriptionView);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
        return menuBar;
    }

    @Override
    public void start(Stage stage) throws ProjectException, FileNotFoundException {
        stage.initStyle(StageStyle.UNDECORATED);
        Theme theme = Theme.getTheme();
        theme.setStyle(Style.DARK);

        Image iconImage = new Image(
            this.getClass().getResource("mklego-icon128.png").toExternalForm());
        stage.getIcons().add(iconImage);
        stage.setTitle("MKLego - Desktop App");

        // --- Create RecentGrid Example ---
        Path rootPath = Path.of("mklego-save-projects");
        ProjectManager manager = new ProjectManager(rootPath);

        ObservableList<RecentItem> recentItems = new MappedList<>(
            manager.projectsProperty(),
            project -> new RecentItem(theme, project));

        AlertQueue queue = new AlertQueue();
        StackPane totalPane = new StackPane();

        BorderlessScene scene = new BorderlessScene(queue, stage, theme, totalPane, 640, 480);

        RecentGrid recentGrid = new RecentGrid(recentItems, project -> {
            try {
                queue.pushBack(new SimpleAlert(AlertType.INFO, "Opening " + project.getName())
                    .withSource("RecentGrid"));

                LGCode code = ProjectConverter.createCode(project);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                code.writeText(outputStream);

                Scene3D scene3d = new Scene3D(theme, project.getScene(), 0, 0);
                EditingController editing = new EditingController();
                editing.control(scene3d);
                scene3d.bindSizeToContainer(totalPane);

                // === HELP OVERLAY =============================================
                queue.pushBack(new SimpleAlert(AlertType.INFO, "PRESS H FOR HELP")
                    .withSource("EditingController"));
                StackPane helpOverlay = new StackPane();
                helpOverlay.setStyle(
                    "-fx-background-color: rgba(0,0,0,0.75);" +
                    "-fx-padding: 40px;");
                helpOverlay.setVisible(false);

                var helpText = new javafx.scene.control.Label(
                    """
                    Controls
                    -----------------------------------------
                    Camera:
                    • O – Orbit mode
                    • P – Pan mode
                    
                    Editing:
                    • S – Select mode (highlight a brick)
                    • D – Delete mode
                    • A – Add mode
                    • R – Rotate preview brick
                    • 1 – Select 2×4 brick
                    • 2 – Select 2×2 brick
                    
                    Misc:
                    • ESC – Exit current mode
                    • H – Toggle help panel
                    """
                );
                helpText.setStyle(
                    "-fx-font-size: 18px;" +
                    "-fx-text-fill: white;" +
                    "-fx-font-family: 'Consolas';");

                helpOverlay.getChildren().add(helpText);
                totalPane.getChildren().add(scene3d);
                totalPane.getChildren().add(helpOverlay);

                // === KEY HANDLERS ============================================
                scene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.H) {
                        helpOverlay.setVisible(!helpOverlay.isVisible());
                    }

                    else if (event.getCode() == KeyCode.ESCAPE) {
                        helpOverlay.setVisible(false);
                        scene3d.getCameraController().getPanController().setEnabled(false);
                        scene3d.getCameraController().getOrbitController().setEnabled(false);
                        editing.setEnabled(false);
                        editing.setMode(EditingController.Mode.SELECT);
                    }

                    else if (event.getCode() == KeyCode.O) {
                        helpOverlay.setVisible(false);
                        scene3d.getCameraController().getPanController().setEnabled(false);
                        scene3d.getCameraController().getOrbitController().setEnabled(true);
                        editing.setEnabled(false);
                        editing.setMode(EditingController.Mode.SELECT);
                    }

                    else if (event.getCode() == KeyCode.P) {
                        helpOverlay.setVisible(false);
                        scene3d.getCameraController().getOrbitController().setEnabled(false);
                        scene3d.getCameraController().getPanController().setEnabled(true);
                        editing.setEnabled(false);
                        editing.setMode(EditingController.Mode.SELECT);
                    }

                    else if (event.getCode() == KeyCode.S) {
                        helpOverlay.setVisible(false);
                        scene3d.getCameraController().getPanController().setEnabled(false);
                        scene3d.getCameraController().getOrbitController().setEnabled(false);
                        editing.setEnabled(true);
                        editing.setMode(EditingController.Mode.SELECT);
                        try {
                            queue.pushBack(new SimpleAlert(AlertType.INFO,
                                    "Select mode activated").withSource("EditingController"));
                        } catch (AlertAlreadyExistsException ex) {}
                    }

                    else if (event.getCode() == KeyCode.D) {
                        helpOverlay.setVisible(false);
                        scene3d.getCameraController().getPanController().setEnabled(false);
                        scene3d.getCameraController().getOrbitController().setEnabled(false);
                        editing.setEnabled(true);
                        editing.setMode(EditingController.Mode.DELETE);
                        try {
                            queue.pushBack(new SimpleAlert(AlertType.INFO,
                                    "Delete mode activated").withSource("EditingController"));
                        } catch (AlertAlreadyExistsException ex) {}
                    }

                    else if (event.getCode() == KeyCode.A) {
                        helpOverlay.setVisible(false);
                        scene3d.getCameraController().getPanController().setEnabled(false);
                        scene3d.getCameraController().getOrbitController().setEnabled(false);
                        editing.setEnabled(true);
                        editing.setMode(EditingController.Mode.ADD);
                        try {
                            queue.pushBack(new SimpleAlert(AlertType.INFO,
                                    "Add mode activated").withSource("EditingController"));
                        } catch (AlertAlreadyExistsException ex) {}
                    }

                    else if (event.getCode() == KeyCode.R) {
                        if (editing.getMode() == EditingController.Mode.ADD) {
                            editing.rotatePreview();
                        }
                    }

                    else if (event.getCode() == KeyCode.DIGIT1) {
                        if (editing.getMode() == EditingController.Mode.ADD) {
                            editing.setCurrentAddKind(new StdLegoPieceKind(2, 4));
                        }
                    }

                    else if (event.getCode() == KeyCode.DIGIT2) {
                        if (editing.getMode() == EditingController.Mode.ADD) {
                            editing.setCurrentAddKind(new StdLegoPieceKind(2, 2));
                        }
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, theme);

        totalPane.getChildren().add(recentGrid);

        AlertPane pane = new AlertPane(queue, theme);
        theme.useBackground(totalPane);

        scene.setIcon(iconImage);
        scene.addLayer(pane);

        MenubarIcon icon = new MenubarIcon();
        icon.setIcon(iconImage);
        scene.setMenuBar(exampleMenuBar(icon.render()));


        theme.setScene(scene);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
