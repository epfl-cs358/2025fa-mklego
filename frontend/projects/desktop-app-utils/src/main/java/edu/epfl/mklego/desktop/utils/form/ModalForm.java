package edu.epfl.mklego.desktop.utils.form;

import edu.epfl.mklego.desktop.utils.Theme;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import jfxtras.styles.jmetro.MDL2IconFont;

public abstract class ModalForm extends StackPane {

    private void close () {
        ModalFormContainer.getInstance().setForm(null);
    }
    public void cancel () {
        onCancel();
        close();
    }
    public void submit () {
        if (onSubmit())
            close();
    }

    public abstract void    onCancel ();
    public abstract void    onStart  ();
    public abstract boolean onSubmit ();

    public abstract StringProperty titleProperty ();

    public abstract VBox render ();

    private StringProperty errorProperty = new SimpleStringProperty("");
    public StringProperty errorProperty () {
        return errorProperty;
    }
    public void setError (String error) {
        errorProperty.set(error);
    }
    public String getError () {
        return errorProperty.get();
    }

    public ModalForm () {
        super();

        Pane shadowPane = new Pane();
        shadowPane.setBackground(Background.fill(Color.web("#00000044")));
        shadowPane.setOnMouseClicked(event -> close());

        Pane contentPane = new Pane();
        contentPane.setPrefWidth(400);
        Theme.getTheme().useBackground(contentPane);

        HBox horizontalPane = new HBox();
        horizontalPane.setAlignment(Pos.CENTER);
        horizontalPane.setPickOnBounds(false);
        horizontalPane.getChildren().add(contentPane);
        VBox containerPane  = new VBox();
        containerPane.setAlignment(Pos.CENTER);
        containerPane.setPickOnBounds(false);
        containerPane.getChildren().add(horizontalPane);

        VBox trueContent = new VBox();
        trueContent.setPrefWidth(400);
        trueContent.setPadding(new Insets(20));
        trueContent.setSpacing(10);
        contentPane.getChildren().add(trueContent);

        Label title = new Label();
        title.textProperty().bind(titleProperty());
        title.setFont(new Font(20));

        MDL2IconFont closeIcon = new MDL2IconFont("\uE8BB");

        VBox closeBox = new VBox(closeIcon);
        closeBox.setPrefWidth (20);
        closeBox.setPrefHeight(20);
        closeBox.setAlignment(Pos.CENTER);
        closeBox.setCursor(Cursor.HAND);

        closeBox.setOnMouseClicked(event -> close());

        Region titleSpacer = new Region();
        HBox titleBox = new HBox(title, titleSpacer, closeBox);
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        titleBox.setAlignment(Pos.CENTER);
        trueContent.getChildren().add(titleBox);

        this.getChildren()
            .addAll(shadowPane, containerPane);

        Button submitButton = new Button("Submit");
        Region submitSpacer = new Region();
        Label errorLabel = new Label();
        errorLabel.textProperty().bind(errorProperty);
        errorLabel.textFillProperty().bind(
            Theme.getTheme().makeBinding(
                Color.web("#d82629"),
                Color.web("#ce4648"))
        );
        HBox submitBox = new HBox(submitSpacer, errorLabel, submitButton);
        submitBox.setSpacing(10);
        submitBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(submitSpacer, Priority.ALWAYS);
        submitButton.setCursor(Cursor.HAND);
        
        submitButton.setOnMouseClicked(event -> submit());
        
        VBox renderedBox = render();
        renderedBox.setSpacing(10);
        trueContent.getChildren().add(renderedBox);

        trueContent.getChildren().add(submitBox);

        onStart();
    }

}
