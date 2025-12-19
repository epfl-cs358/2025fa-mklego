package edu.epfl.mklego.desktop.home;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import edu.epfl.mklego.desktop.utils.form.FileField;
import edu.epfl.mklego.desktop.utils.form.IntegerTextField;
import edu.epfl.mklego.desktop.utils.form.ModalForm;
import edu.epfl.mklego.lxfml.LXFMLReader;
import edu.epfl.mklego.project.Project;
import edu.epfl.mklego.project.ProjectException;
import edu.epfl.mklego.project.ProjectManager;
import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import edu.epfl.mklego.slicer.Slicer;

public class ImportProjectForm extends ModalForm {

    /* ======================= IMPORT FACTORIES ======================= */

    private static abstract class ImportProjectFactory {
        public static ImportProjectFactory getFactoryForFile(String extension) {
            switch (extension) {
                case "lxfml":
                    return new ImportLXFMLProject();
                case "stl":
                    return new ImportSTLProject();
            }
            return null;
        }

        public abstract Project create(
            ProjectManager manager,
            String name,
            Path path,
            int numberRows,
            int numberColumns,
            File file
        ) throws IOException, ProjectException;
    }

    private static class ImportLXFMLProject extends ImportProjectFactory {
        @Override
        public Project create(
            ProjectManager manager,
            String name,
            Path path,
            int numberRows,
            int numberColumns,
            File file
        ) throws IOException, ProjectException {

            FileInputStream stream = new FileInputStream(file);
            LegoAssembly assembly =
                LXFMLReader.createAssembly(stream, numberRows, numberColumns);

            ProjectScene scene = ProjectScene.createSceneFrom(name, assembly);

            return manager.createProject(path, name, numberRows, numberColumns, scene);
        }
    }

    private static class ImportSTLProject extends ImportProjectFactory {
        @Override
        public Project create(
            ProjectManager manager,
            String name,
            Path path,
            int numberRows,
            int numberColumns,
            File file
        ) throws IOException, ProjectException {

            LegoAssembly assembly =
                Slicer.pipeline(file, numberRows, numberColumns);

            ProjectScene scene = ProjectScene.createSceneFrom(name, assembly);

            return manager.createProject(path, name, numberRows, numberColumns, scene);
        }
    }

    /* ======================= INSTANCE STATE ======================= */

    private final ProjectManager manager;
    private final Stage stage;

    public ImportProjectForm(Stage stage, ProjectManager manager) {
        this.manager = manager;
        this.stage = stage;
    }

    /* ======================= VALIDATION ======================= */

    private char validateString(String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isLetterOrDigit(c)) continue;
            if (c == ' ' || c == '-') continue;
            return c;
        }
        return 0;
    }

    /* ======================= FORM LIFECYCLE ======================= */

    @Override
    public void onCancel() {}

    @Override
    public void onStart() {}

    @Override
    public boolean onSubmit() {

        String name = projectName.getText().strip();
        String path = projectPath.getText().strip();

        if (name.isEmpty()) {
            setError("Project name shouldn't be empty");
            return false;
        }

        char invalidName = validateString(name);
        if (invalidName != 0) {
            setError("Invalid character '" + invalidName + "' in name");
            return false;
        }

        if (path.isEmpty()) {
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

        if (!plateNumberRows.hasValue() || !plateNumberColumns.hasValue()) {
            setError("Plate dimensions must be provided");
            return false;
        }

        int rows = plateNumberRows.getValue();
        int cols = plateNumberColumns.getValue();

        if (rows <= 0 || rows % 2 != 0) {
            setError("Plate Number Rows must be positive and even");
            return false;
        }

        if (cols <= 0 || cols % 2 != 0) {
            setError("Plate Number Columns must be positive and even");
            return false;
        }

        if (fileField.getFile() != null && factoryProperty.get() == null) {
            setError("Unrecognized file extension");
            return false;
        }

        try {
            if (fileField.getFile() == null) {
                ProjectScene scene =
                    ProjectScene.createEmptyScene(name, rows, cols);

                manager.createProject(resolved, name, rows, cols, scene);

            } else {
                factoryProperty.get().create(
                    manager,
                    name,
                    resolved,
                    rows,
                    cols,
                    fileField.getFile()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            setError("Unexpected error");
            return false;
        }

        return true;
    }

    /* ======================= UI ======================= */

    private static final StringProperty titleProperty =
        new SimpleStringProperty("New Project");

    private TextField projectName;
    private TextField projectPath;
    private IntegerTextField plateNumberRows;
    private IntegerTextField plateNumberColumns;
    private FileField fileField;
    private ObjectProperty<ImportProjectFactory> factoryProperty;

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

        fileField = new FileField(stage, "Import stl or lxfml file (Optional)");
        fileField.setFocusTraversable(false);
        fileField.setMaxWidth(Double.MAX_VALUE);

        HBox fileBox = new HBox(fileField);
        HBox.setHgrow(fileField, Priority.ALWAYS);

        factoryProperty = new SimpleObjectProperty<>();
        factoryProperty.bind(Bindings.createObjectBinding(
            () -> {
                if (fileField.fileProperty().get() == null) return null;
                String ext = FilenameUtils.getExtension(
                    fileField.fileProperty().get().getName()
                );
                return ImportProjectFactory.getFactoryForFile(ext);
            },
            fileField.fileProperty()
        ));

        return new VBox(
            projectName,
            projectPath,
            plateNumberRows,
            plateNumberColumns,
            fileBox
        );
    }
}
