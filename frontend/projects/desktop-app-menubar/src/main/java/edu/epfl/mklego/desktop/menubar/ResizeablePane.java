package edu.epfl.mklego.desktop.menubar;

import edu.epfl.mklego.desktop.menubar.BorderlessController.HorizontalResize;
import edu.epfl.mklego.desktop.menubar.BorderlessController.VerticalResize;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class ResizeablePane extends BorderPane {
    public static final int BORDER_SIZE = 5;

    private final BorderlessController controller;
    
    Node createNode (HorizontalResize hori, VerticalResize vert, Cursor cursor) {
        Pane pane = new Pane();
        pane.setCursor(cursor);

        if (hori != HorizontalResize.NONE)
            pane.setPrefWidth(BORDER_SIZE);
    
        controller.registerResizeGroup(pane, hori, vert);
        return pane;
    }

    public ResizeablePane (BorderlessController controller) {
        super();
        this.controller = controller;

        Pane pane = new Pane();
        pane.setPickOnBounds(false);
        setPickOnBounds(false);

        setCenter(pane);
        setLeft  (createNode(HorizontalResize.LEFT,  VerticalResize.NONE, Cursor.W_RESIZE));
        setRight (createNode(HorizontalResize.RIGHT, VerticalResize.NONE, Cursor.E_RESIZE));

        HBox topBox = new HBox();
        topBox.setPrefHeight(BORDER_SIZE);
        topBox.getChildren().add(
            createNode(HorizontalResize.LEFT, VerticalResize.TOP, Cursor.NW_RESIZE) );
        Node topNode = createNode(HorizontalResize.NONE, VerticalResize.TOP, Cursor.N_RESIZE);
        HBox.setHgrow(topNode, Priority.ALWAYS);
        topBox.getChildren().add(topNode);
        topBox.getChildren().add(
            createNode(HorizontalResize.RIGHT, VerticalResize.TOP, Cursor.NE_RESIZE) );
        setTop(topBox);
        
        HBox bottomBox = new HBox();
        bottomBox.setPrefHeight(BORDER_SIZE);
        bottomBox.getChildren().add(
            createNode(HorizontalResize.LEFT, VerticalResize.BOTTOM, Cursor.SW_RESIZE) );
        Node bottomNode = createNode(HorizontalResize.NONE, VerticalResize.BOTTOM, Cursor.S_RESIZE);
        HBox.setHgrow(bottomNode, Priority.ALWAYS);
        bottomBox.getChildren().add(bottomNode);
        bottomBox.getChildren().add(
            createNode(HorizontalResize.RIGHT, VerticalResize.BOTTOM, Cursor.SE_RESIZE) );
        setBottom(bottomBox);
    }
}
