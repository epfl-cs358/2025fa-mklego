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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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

    private static final String BTN_NORMAL =
    "-fx-background-radius: 3px;" +
    "-fx-background-insets: 0;" +
    "-fx-padding: 0;";

private static final String BTN_SELECTED =
    "-fx-background-radius: 3px;" +
    "-fx-background-insets: 0;" +
    "-fx-padding: 0;" +
    "-fx-border-color: white;" +
    "-fx-border-width: 2px;" +
    "-fx-border-radius: 3px;";


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
        
        EditingController editing = new EditingController();

        BorderlessScene scene = new BorderlessScene(queue, stage, theme, totalPane, 640, 480);

        RecentGrid recentGrid = new RecentGrid(recentItems, project -> {
            try {
                queue.pushBack(new SimpleAlert(AlertType.INFO, "Opening " + project.getName())
                    .withSource("RecentGrid"));

                LGCode code = ProjectConverter.createCode(project);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                code.writeText(outputStream);

                Scene3D scene3d = new Scene3D(theme, project.getScene(), 0, 0);
                editing.control(scene3d);
                scene3d.bindSizeToContainer(totalPane);


        // === COLOR PALETTE UI (compact & fixed size) ====================
        HBox colorPanel = new HBox(4);
        colorPanel.setStyle(
            "-fx-background-color: rgba(20,20,20,0.9);" +
            "-fx-padding: 4px;" +
            "-fx-background-radius: 6px;"
        );

        Color[] colors = {
            Color.RED,
            Color.DEEPSKYBLUE,
            Color.YELLOW,
            Color.GREEN,
            Color.ORANGE,
            Color.PURPLE,
            Color.WHITE,
            Color.BLACK,
            Color.color(0.5, 0.5, 0.5, 0.3) // TRANSPARENT SUPPORT
        };
        final javafx.scene.control.Button[] selectedColorButton = new javafx.scene.control.Button[1];
        for (Color c : colors) {
            Button b = new Button();
            b.setPrefSize(20, 20);
            b.setMinSize(20, 20);
            b.setMaxSize(20, 20);
            b.setFocusTraversable(false);
            b.setStyle(
                BTN_NORMAL +
                "-fx-background-color: rgb("
                    + (int)(c.getRed() * 255) + ","
                    + (int)(c.getGreen() * 255) + ","
                    + (int)(c.getBlue() * 255) + ");"
            );

            b.setOnAction(e -> {
                // Update editor
                editing.setCurrentColor(c);

                // Unselect previous
                if (selectedColorButton[0] != null) {
                    selectedColorButton[0].setStyle(
                        BTN_NORMAL +
                        selectedColorButton[0].getStyle() 
                            .replace(BTN_SELECTED, "")
                    );
                }

                // Select this one
                b.setStyle(
                    BTN_SELECTED +
                    "-fx-background-color: rgb("
                        + (int)(c.getRed() * 255) + ","
                        + (int)(c.getGreen() * 255) + ","
                        + (int)(c.getBlue() * 255) + ");"
                );

                selectedColorButton[0] = b;
            });

            colorPanel.getChildren().add(b);
        }
        // Preselect default color (first one, or match currentAddColor)
        if (!colorPanel.getChildren().isEmpty()) {
            Button defaultBtn = (Button) colorPanel.getChildren().get(0);

            // Apply selected style
            defaultBtn.setStyle(
                BTN_SELECTED +
                defaultBtn.getStyle().replace(BTN_NORMAL, "")
            );

            selectedColorButton[0] = defaultBtn;
        }



        //prevent StackPane stretching
        colorPanel.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        colorPanel.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        colorPanel.setPickOnBounds(false);

        // Position in corner
        StackPane.setAlignment(colorPanel, Pos.TOP_RIGHT);
        StackPane.setMargin(colorPanel, new Insets(12));

        // Start hidden
        colorPanel.setVisible(true);




        // === SIZE PALETTE UI (ADD MODE) ================================
        HBox sizePanel = new HBox(4);
        sizePanel.setStyle(
            "-fx-background-color: rgba(20,20,20,0.9);" +
            "-fx-padding: 4px;" +
            "-fx-background-radius: 6px;"
        );

        // LEGO sizes (rows × columns)
        StdLegoPieceKind[] sizes = {
            new StdLegoPieceKind(2, 2),
            new StdLegoPieceKind(2, 3),
            new StdLegoPieceKind(2, 4),
        };

        // Track selected size button (same pattern as color)
        final Button[] selectedSizeButton = new Button[1];

        for (StdLegoPieceKind kind : sizes) {
            Button b = new Button(
                kind.getNumberRows() + "×" + kind.getNumberColumns()
            );

            b.setFocusTraversable(false);
            b.setStyle(
                BTN_NORMAL +
                "-fx-font-size: 11px;" +
                "-fx-padding: 2 6 2 6;" +
                "-fx-background-radius: 4px;"
            );

            b.setOnAction(e -> {
                editing.setCurrentAddKind(kind);

                if (selectedSizeButton[0] != null) {
                    selectedSizeButton[0].setStyle(
                        BTN_NORMAL +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 2 6 2 6;" +
                        "-fx-background-radius: 4px;"
                    );
                }

                b.setStyle(
                    BTN_SELECTED +
                    "-fx-border-color: #ffffffff;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 2 6 2 6;" +
                    "-fx-background-radius: 4px;"
                );

                selectedSizeButton[0] = b;
            });

            // 🔵 PRESELECT DEFAULT SIZE (2×4)
            if (kind.getNumberRows() == 2 && kind.getNumberColumns() == 4) {
                b.setStyle(
                    BTN_SELECTED +
                    "-fx-border-color: #ffffffff;" +
                    "-fx-font-size: 11px;" +
                    "-fx-padding: 2 6 2 6;" +
                    "-fx-background-radius: 4px;"
                );
                selectedSizeButton[0] = b;
            }

            sizePanel.getChildren().add(b);
        }


        // Prevent StackPane stretching
        sizePanel.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        sizePanel.setPrefSize(StackPane.USE_COMPUTED_SIZE, StackPane.USE_COMPUTED_SIZE);
        sizePanel.setPickOnBounds(false);

        // Position below color panel
        StackPane.setAlignment(sizePanel, Pos.TOP_RIGHT);
        StackPane.setMargin(sizePanel, new Insets(44, 12, 12, 12));

        // Start visible (or control via mode if you want)
        sizePanel.setVisible(true);




        // === SUPPORT PIECES TOGGLE ================================

        String SUPPORT_TOGGLE_OFF =
            "-fx-background-radius: 4px;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 2 6 2 6;" +
            "-fx-font-size: 11px;" +
            "-fx-background-color: #3a3a3a;" +
            "-fx-text-fill: white;";

        String SUPPORT_TOGGLE_ON =
            "-fx-background-radius: 4px;" +
            "-fx-background-insets: 0;" +
            "-fx-padding: 2 6 2 6;" +
            "-fx-font-size: 11px;" +
            "-fx-background-color: #2196f3;" +
            "-fx-text-fill: white;";



        ToggleButton supportToggle = new ToggleButton("Hide Supports");
        supportToggle.setFocusTraversable(false);
        supportToggle.setSelected(false); // false = supports visible

        supportToggle.setStyle(SUPPORT_TOGGLE_OFF);

        supportToggle.selectedProperty().addListener((obs, oldV, selected) -> {
            // selected = hide supports
            scene3d.setSupportPiecesVisible(!selected);

            supportToggle.setStyle(
                selected ? SUPPORT_TOGGLE_ON : SUPPORT_TOGGLE_OFF
            );
        });


        supportToggle.setOnMouseEntered(e ->
            supportToggle.setOpacity(0.85)
        );
        supportToggle.setOnMouseExited(e ->
            supportToggle.setOpacity(1.0)
        );

        VBox sizeAndSupportPanel = new VBox(6);
        sizeAndSupportPanel.setAlignment(Pos.TOP_RIGHT);
        sizeAndSupportPanel.getChildren().addAll(sizePanel, supportToggle);

        sizeAndSupportPanel.setMaxSize(StackPane.USE_PREF_SIZE, StackPane.USE_PREF_SIZE);
        sizeAndSupportPanel.setPickOnBounds(false);

        StackPane.setAlignment(sizeAndSupportPanel, Pos.TOP_RIGHT);
        StackPane.setMargin(sizeAndSupportPanel, new Insets(44, 12, 12, 12));





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
                    • S – Select mode
                    • D – Delete mode
                    • A – Add mode
                    • R – Rotate preview brick
                    
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
                totalPane.getChildren().add(colorPanel);
                totalPane.getChildren().add(sizeAndSupportPanel);
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
                    }

                    else if (event.getCode() == KeyCode.D) {
                        helpOverlay.setVisible(false);
                        scene3d.getCameraController().getPanController().setEnabled(false);
                        scene3d.getCameraController().getOrbitController().setEnabled(false);
                        editing.setEnabled(true);
                        editing.setMode(EditingController.Mode.DELETE);
                    }

                    else if (event.getCode() == KeyCode.A) {
                        helpOverlay.setVisible(false);
                        scene3d.getCameraController().getPanController().setEnabled(false);
                        scene3d.getCameraController().getOrbitController().setEnabled(false);
                        editing.setEnabled(true);
                        editing.setMode(EditingController.Mode.ADD);
                    }

                    else if (event.getCode() == KeyCode.R) {
                        if (editing.getMode() == EditingController.Mode.ADD) {
                            editing.rotatePreview();
                        }
                    }

                    /*else if (event.getCode() == KeyCode.DIGIT1) {
                        if (editing.getMode() == EditingController.Mode.ADD) {
                            editing.setCurrentAddKind(new StdLegoPieceKind(2, 4));
                        }
                    }

                    else if (event.getCode() == KeyCode.DIGIT2) {
                        if (editing.getMode() == EditingController.Mode.ADD) {
                            editing.setCurrentAddKind(new StdLegoPieceKind(2, 2));
                        }
                    }else if (event.getCode() == KeyCode.DIGIT3) {
                        editing.setCurrentColor(Color.RED);
                    }
                    else if (event.getCode() == KeyCode.DIGIT4) {
                        editing.setCurrentColor(Color.BLUE);
                    }
                    else if (event.getCode() == KeyCode.DIGIT5) {
                        editing.setCurrentColor(Color.YELLOW);
                    }
                    else if (event.getCode() == KeyCode.DIGIT6) {
                        editing.setCurrentColor(Color.PURPLE);
                    }
                    else if (event.getCode() == KeyCode.DIGIT7) {
                        editing.setCurrentColor(Color.WHITE);
                    }
                    else if (event.getCode() == KeyCode.DIGIT8) {
                        editing.setCurrentColor(Color.BLACK);
                    } else if (event.getCode() == KeyCode.DIGIT9) {
                        editing.setCurrentColor(Color.color(0.5, 0.5, 0.5, 0.3)); // SUPPORT
                    }*/

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

        ModalFormContainer container = ModalFormContainer.getInstance();
        PauseTransition tr = new PauseTransition(Duration.seconds(5));
        tr.setOnFinished(event -> container.setForm(new ImportProjectForm(stage, manager)));
        tr.play();
        scene.addLayer(container);

        theme.setScene(scene);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
