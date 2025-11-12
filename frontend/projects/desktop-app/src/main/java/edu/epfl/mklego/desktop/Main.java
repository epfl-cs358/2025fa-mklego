package edu.epfl.mklego.desktop;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import edu.epfl.mklego.desktop.alerts.AlertPane;
import edu.epfl.mklego.desktop.alerts.AlertQueue;
import edu.epfl.mklego.desktop.alerts.SimpleAlert;
import edu.epfl.mklego.desktop.alerts.SimpleAlert.AlertButton;
import edu.epfl.mklego.desktop.alerts.SimpleAlert.AlertButtonType;
import edu.epfl.mklego.desktop.alerts.SimpleAlert.AlertType;
import edu.epfl.mklego.desktop.alerts.exceptions.AlertAlreadyExistsException;
import edu.epfl.mklego.desktop.home.ImportProjectForm;
import edu.epfl.mklego.desktop.home.NewProjectForm;
import edu.epfl.mklego.desktop.home.RecentGrid;
import edu.epfl.mklego.desktop.home.model.RecentItem;
import edu.epfl.mklego.desktop.menubar.BorderlessScene;
import edu.epfl.mklego.desktop.menubar.MenubarIcon;
import edu.epfl.mklego.desktop.render.Scene3D;
import edu.epfl.mklego.desktop.utils.MappedList;
import edu.epfl.mklego.desktop.utils.Theme;
import edu.epfl.mklego.desktop.utils.form.ModalFormContainer;
import edu.epfl.mklego.lxfml.LXFMLReader;
import edu.epfl.mklego.project.ProjectException;
import edu.epfl.mklego.project.ProjectManager;
import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.Transform;
import edu.epfl.mklego.project.scene.entities.GroupEntity;
import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;

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
        CheckMenuItem titleView = createMenuItem ("Title");
        CheckMenuItem binNameView = createMenuItem ("Binomial name");
        CheckMenuItem picView = createMenuItem ("Picture");
        CheckMenuItem descriptionView = createMenuItem (
                "Decsription");

        menuView.getItems().addAll(titleView, binNameView, picView,
                descriptionView);
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);

        return menuBar;
    }

    @Override
    public void start(Stage stage) throws ProjectException, FileNotFoundException {
        stage.initStyle(StageStyle.UNDECORATED);
        Theme theme = Theme.getTheme();
        theme.setStyle(Style.DARK);
        //Scene3D subscene = new Scene3D(theme, null,400, 400);
        //
        //Pane subScenePane = new Pane(subscene);
        //subscene.bindSizeToContainer(subScenePane);

        Image iconImage = new Image(
            this.getClass().getResource("mklego-icon128.png").toExternalForm());
        stage.getIcons().add(iconImage);
        stage.setTitle("MKLego - Desktop App");


        // --- Create RecentGrid Example ---
        Path rootPath = Path.of("mklego-save-projects");
        ProjectManager manager = new ProjectManager(rootPath);

        LegoAssembly asm = LXFMLReader.createAssembly(new FileInputStream("C:/Users/theoh/Downloads/lxfmltext2.lxfml"), 22, 22);

        ObservableList<RecentItem> recentItems = new MappedList<>(
            manager.projectsProperty(), 
            project -> new RecentItem(theme, project));
        
        AlertQueue queue = new AlertQueue();
        StackPane totalPane = new StackPane();
        BorderlessScene scene = new BorderlessScene(queue, stage, theme, totalPane, 640, 480);
        RecentGrid recentGrid = new RecentGrid(recentItems, path -> {
            System.out.println("Opening file: " + path.getName());
            try {
                queue.pushBack(new SimpleAlert(AlertType.INFO, "Opening " + path.getName()).withSource("RecentGrid"));
            
                Scene3D scene3d = new Scene3D(theme, new ProjectScene(new GroupEntity(new Transform(), "", List.of()), asm), 0, 0);
                scene3d.bindSizeToContainer(totalPane);

                scene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        scene3d.getCameraController()
                            .getPanController()
                            .setEnabled(false);
                        scene3d.getCameraController()
                            .getOrbitController()
                            .setEnabled(false);
                    } else if (event.getCode() == KeyCode.O) {
                        scene3d.getCameraController()
                            .getPanController()
                            .setEnabled(false);
                        scene3d.getCameraController()
                            .getOrbitController()
                            .setEnabled(true);
                    } else if (event.getCode() == KeyCode.P) {
                        scene3d.getCameraController()
                            .getOrbitController()
                            .setEnabled(false);
                        scene3d.getCameraController()
                            .getPanController()
                            .setEnabled(true);
                    }
                });
                totalPane.getChildren().add(scene3d);
            } catch (AlertAlreadyExistsException e) {
                e.printStackTrace();
            }
        }, theme);
        totalPane.getChildren().add(recentGrid);

        //StackPane totalPane = new StackPane(recentGrid);
        AlertPane pane = new AlertPane(queue, theme);
        theme.useBackground(totalPane);

        scene.setIcon(iconImage);
        scene.addLayer(pane);
        
        ModalFormContainer container = ModalFormContainer.getInstance();
        PauseTransition tr = new PauseTransition(Duration.seconds(5));
        tr.setOnFinished(event -> container.setForm(new NewProjectForm(manager)));
        tr.play();
        scene.addLayer(container);

        MenubarIcon icon = new MenubarIcon();
        icon.setIcon(iconImage);
        scene.setMenuBar(exampleMenuBar(icon.render()));

        /*scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                subscene.getCameraController()
                    .getPanController()
                    .setEnabled(false);
                subscene.getCameraController()
                    .getOrbitController()
                    .setEnabled(false);
            } else if (event.getCode() == KeyCode.O) {
                subscene.getCameraController()
                    .getPanController()
                    .setEnabled(false);
                subscene.getCameraController()
                    .getOrbitController()
                    .setEnabled(true);
            } else if (event.getCode() == KeyCode.P) {
                subscene.getCameraController()
                    .getOrbitController()
                    .setEnabled(false);
                subscene.getCameraController()
                    .getPanController()
                    .setEnabled(true);
            }
        });*/
        
        /*try {
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
        }*/

        theme.setScene(scene);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}