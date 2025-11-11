package edu.epfl.mklego.desktop.home;

import java.io.File;
import java.nio.file.Path;

import edu.epfl.mklego.desktop.utils.form.IntegerTextField;
import edu.epfl.mklego.desktop.utils.form.ModalForm;
import edu.epfl.mklego.project.Project;
import edu.epfl.mklego.project.ProjectException;
import edu.epfl.mklego.project.ProjectManager;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ImportProjectForm extends ModalForm {

    private final ProjectManager manager;
    private final Stage stage;
    public ImportProjectForm (Stage stage, ProjectManager manager) {
        this.manager = manager;
        this.stage   = stage;
    }

    private char validateString (String str) {
        for (int idx = 0; idx < str.length(); idx ++) {
            char chr = str.charAt(idx);
            if ('a' <= chr && chr <= 'z') continue ;
            if ('A' <= chr && chr <= 'Z') continue ;
            if ('0' <= chr && chr <= '9') continue ;
            if (chr == ' ') continue ;
            if (chr == '-') continue ;

            return chr;
        }

        return 0;
    }

    @Override
    public void onCancel() {}

    @Override
    public void onStart() {}

    @Override
    public boolean onSubmit () {
        String name = projectName.getText().strip();
        String path = projectPath.getText().strip();

        if (name.equals("")) {
            setError("Project name shouldn't be empty");
            return false;
        }
        char invalidName = validateString(name);
        if (invalidName != 0) {
            setError("Invalid character '" + invalidName + "' in name");
            return false;
        }

        if (path.equals("")) {
            setError("Project path shouldn't be empty");
            return false;
        }
        char invalidPath = validateString(path);
        if (invalidPath != 0) {
            setError("Invalid character '" + invalidPath + "' in path");
            return false;
        }

        Path resolved = manager.projectPath(path);
        if (!manager.canCreateProject(resolved)) {
            setError("Project with path '" + path + "' already exists");
            return false;
        }

        if (!plateNumberRows.hasValue()) {
            setError("Plate Number Rows shouldn't be empty");
            return false;
        }
        if (!plateNumberColumns.hasValue()) {
            setError("Plate Number Columns shouldn't be empty");
            return false;
        }
        int numberRows = plateNumberRows.getValue();
        int numberColumns = plateNumberColumns.getValue();

        if (numberRows <= 0) {
            setError("Plate Number Rows should be positive");
            return false;
        }
        if (numberRows % 2 != 0) {
            setError("Plate Number Rows should be even");
            return false;
        }

        if (numberColumns <= 0) {
            setError("Plate Number Columns should be positive");
            return false;
        }
        if (numberColumns % 2 != 0) {
            setError("Plate Number Columns should be even");
            return false;
        }

        try {
            Project project = manager.createProject(
                resolved,
                name,
                numberRows,
                numberColumns
            );
        } catch (ProjectException e) {
            e.printStackTrace();
            setError("Unexpected error");
            return false;
        }

        return true;
    }

    private static final StringProperty titleProperty = new SimpleStringProperty("New Project");

    private TextField projectName;
    private TextField projectPath;

    private IntegerTextField plateNumberRows;
    private IntegerTextField plateNumberColumns;

    private VBox rendered;

    @Override
    public StringProperty titleProperty() {
        return titleProperty;
    }

    @Override
    public VBox render() {
        projectName = new TextField();
        projectName.setPromptText("Project Name");

        projectPath = new TextField();
        projectPath.setPromptText("Project Path");

        plateNumberRows = new IntegerTextField();
        plateNumberRows.setPromptText("Plate - Number Rows");

        plateNumberColumns = new IntegerTextField();
        plateNumberColumns.setPromptText("Plate - Number Columns");

        PauseTransition tr = new PauseTransition(Duration.seconds(5));
        tr.setOnFinished(event -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
        });
        tr.play();

        rendered = new VBox(
            projectName, 
            projectPath,
            plateNumberRows,
            plateNumberColumns);
        return rendered;
    }
    
}
