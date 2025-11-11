package edu.epfl.mklego.desktop.home;

import edu.epfl.mklego.desktop.home.model.RecentItem;
import edu.epfl.mklego.desktop.utils.Theme;
import edu.epfl.mklego.project.Project;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class RecentGrid extends ScrollPane {

    public final static String LIGHT_RECENT_GRID_CSS = "RecentGrid-Light.css";
    public final static String DARK_RECENT_GRID_CSS  = "RecentGrid-Dark.css";
    public final static String RECENT_GRID_CSS       = "RecentGrid.css";

    private final FlowPane flowPane = new FlowPane();
    private final ObservableList<RecentItem> recentItems;
    private final Consumer<Project> callback;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RecentGrid(ObservableList<RecentItem> recentItems, Consumer<Project> callback, Theme theme) {
        this.recentItems = recentItems;
        this.callback = callback;

        setContent(flowPane);
        setFitToWidth(true);
        setFitToHeight(true);
        setStyle("-fx-background-color: transparent;");

        flowPane.setPadding(new Insets(10));
        flowPane.setHgap(10);
        flowPane.setVgap(10);
        flowPane.getStyleClass().add("recent-grid");

        theme.useResourceStyles(
                getStylesheets(),
                getClass(),
                RECENT_GRID_CSS, LIGHT_RECENT_GRID_CSS, DARK_RECENT_GRID_CSS
        );

        this.recentItems.addListener((ListChangeListener<RecentItem>) c -> {
            flowPane.getChildren().clear();
            for (RecentItem item : recentItems) {
                flowPane.getChildren().add(createItemView(item));
            }
        });

        for (RecentItem item : recentItems) {
            flowPane.getChildren().add(createItemView(item));
        }
    }

    private VBox createItemView(RecentItem item) {
        VBox itemView = new VBox(5);
        itemView.setUserData(item);
        itemView.getStyleClass().add("recent-item-view");

        itemView.setOnMouseClicked(event -> {
            if (callback != null) {
                callback.accept(item.getProject());
            }
        });

        VBox textView = new VBox();
        textView.setPadding(new Insets(0, 5, 5, 5));

        ImageView imageView = new ImageView();
        imageView.imageProperty().bind(item.imageProperty());
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("recent-item-image");

        imageView.setClip(makeClip());

        Label nameLabel = new Label();
        nameLabel.textProperty().bind(item.nameProperty());
        nameLabel.getStyleClass().add("recent-item-name");

        Label dateLabel = new Label();
        item.lastModifiedProperty().addListener((obs, oldDate, newDate) ->
                dateLabel.setText(newDate != null ? newDate.format(DATE_TIME_FORMATTER) : ""));
        dateLabel.setText(item.getLastModified() != null ? item.getLastModified().format(DATE_TIME_FORMATTER) : "");
        dateLabel.getStyleClass().add("recent-item-date");

        itemView.getChildren().addAll(imageView, textView);
        textView.getChildren().addAll(nameLabel, dateLabel);
        return itemView;
    }

    private Path makeClip () {
        Path clip;

        double height  = 150;
        double width   = 150;
        double radius1 = 6;
        double radius2 = 6;
        double radius3 = 0;
        double radius4 = 0;

        clip = new Path(new MoveTo(0, radius1),
                new ArcTo(radius1, radius1, 0, radius1, 0, false, true),
                new HLineTo(width - radius2),
                new ArcTo(radius2, radius2, 0, width, radius2, false, true),
                new VLineTo(height - radius4),
                new ArcTo(radius4, radius4, 0, width - radius4, height, false, true),
                new HLineTo(radius3),
                new ArcTo(radius3, radius3, 0, 0, height - radius3, false, true));

        clip.setFill(Color.ALICEBLUE);

        return clip;
    }
}
