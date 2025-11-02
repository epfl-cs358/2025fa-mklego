package edu.epfl.mklego.desktop.home;

import edu.epfl.mklego.desktop.home.model.RecentItem;
import edu.epfl.mklego.desktop.utils.Theme;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class RecentGrid extends ScrollPane {

    public final static String LIGHT_RECENT_GRID_CSS = "RecentGrid-Light.css";
    public final static String DARK_RECENT_GRID_CSS  = "RecentGrid-Dark.css";
    public final static String RECENT_GRID_CSS       = "RecentGrid.css";

    private final FlowPane flowPane = new FlowPane();
    private final ObservableList<RecentItem> recentItems;
    private final Consumer<Path> callback;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RecentGrid(ObservableList<RecentItem> recentItems, Consumer<Path> callback, Theme theme) {
        this.recentItems = recentItems;
        this.callback = callback;

        setContent(flowPane);
        setFitToWidth(true);
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
        itemView.setPadding(new Insets(5));
        itemView.getStyleClass().add("recent-item-view");

        itemView.setOnMouseClicked(event -> {
            if (callback != null) {
                callback.accept(item.getPath());
            }
        });

        ImageView imageView = new ImageView();
        imageView.imageProperty().bind(item.imageProperty());
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label();
        nameLabel.textProperty().bind(item.nameProperty());
        nameLabel.getStyleClass().add("recent-item-name");

        Label dateLabel = new Label();
        item.lastModifiedProperty().addListener((obs, oldDate, newDate) ->
                dateLabel.setText(newDate != null ? newDate.format(DATE_TIME_FORMATTER) : ""));
        dateLabel.setText(item.getLastModified() != null ? item.getLastModified().format(DATE_TIME_FORMATTER) : "");
        dateLabel.getStyleClass().add("recent-item-date");

        itemView.getChildren().addAll(imageView, nameLabel, dateLabel);
        return itemView;
    }
}
