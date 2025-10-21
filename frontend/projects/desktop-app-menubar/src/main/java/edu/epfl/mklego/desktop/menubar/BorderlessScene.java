package edu.epfl.mklego.desktop.menubar;

import edu.epfl.mklego.desktop.alerts.AlertQueue;
import edu.epfl.mklego.desktop.utils.Theme;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.MDL2IconFont;

public class BorderlessScene extends Scene {

    private final Stage      stage;
    private final Theme      theme;
    private final StackPane  rootPane;
    private final BorderPane content = new BorderPane();

    private final MenubarIcon icon = new MenubarIcon();
    private final VBox menuBarVBox = new VBox(); 

    private final BorderlessController controller;

    public void setIcon (Image icon) {
        this.icon.setIcon(icon);
    }
    public void setMain (Parent root) {
        content.setCenter(root);
    }
    public void setMenuBar (MenuBar menu) {
        menuBarVBox.getChildren().clear();

        if (menu == null) return ;
        menuBarVBox.getChildren().add(menu);
    }


    Node createIcon (StringProperty text, String... classes) {
        MDL2IconFont content = new MDL2IconFont();
        content.textProperty().bind(text);

        VBox box = new VBox(content);
        box.setPrefWidth (36);
        box.setPrefHeight(36);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().addAll(classes);
        
        return box;
    }
    Node createIcon (String text, String... classes) {
        return createIcon(new SimpleStringProperty(text), classes);
    }
    void setup (Parent root) {
        ResizeablePane resizeablePane = new ResizeablePane(controller);

        rootPane.getChildren().addAll(content, resizeablePane);
        content.setCenter(root);
        
        HBox topContent = new HBox();
        controller.registerTopBar(topContent);

        topContent.setPrefHeight(36);
        topContent.getStyleClass().add("top-menu-bar");
        theme.useBackground(topContent);
        theme.useResourceStyles(
            getStylesheets(),
            getClass(), 
            "Menubar.css", 
            "Menubar-Light.css",
            "Menubar-Dark.css"
        );

        Node icon = this.icon.render();
        HBox.setMargin(icon, new Insets(8));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Node minimize = createIcon("\uE921", "icon-gray");

        StringProperty maximizeIcon = new SimpleStringProperty();
        maximizeIcon.bind(
            Bindings.when(controller.isMaximizedProperty())
                .then("\uE923")
                .otherwise("\uE922")
        );
        Node maximize = createIcon(maximizeIcon, "icon-gray");
        Node close    = createIcon("\uE8BB", "icon-red");

        minimize.setOnMouseClicked(evt -> stage.setIconified(true));
        maximize.setOnMouseClicked(evt -> controller.setIsMaximized(!controller.isMaximized()));
        close.setOnMouseClicked(evt -> stage.close());

        topContent.getChildren().addAll(icon, menuBarVBox, spacer, minimize, maximize, close);
        
        content.setTop(topContent);
    }

    public BorderlessScene(AlertQueue queue, Stage primaryStage, Theme theme, Parent root, int width, int height) {
        super(new StackPane(), width, height);
        rootPane = (StackPane) getRoot();
        stage = primaryStage;
        controller = new BorderlessController(primaryStage, queue);
        this.theme = theme;

        setup(root);
    }
    public BorderlessScene(AlertQueue queue, Stage primaryStage, Theme theme, Parent root) {
        super(new StackPane());
        rootPane = (StackPane) getRoot();
        stage = primaryStage;
        controller = new BorderlessController(primaryStage, queue);
        this.theme = theme;

        setup(root);
    }
    
}
