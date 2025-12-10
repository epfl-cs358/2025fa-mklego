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

    private static abstract class ImportProjectFactory {
        public static ImportProjectFactory getFactoryForFile (String extension) {
            switch (extension) {
                case "lxfml":
                    return new ImportLXFMLProject();
                case "stl":
                    return new ImportSTLProject();
            }

            return null;
        }

        public abstract Project create (
            ProjectManager manager, String name, Path path, 
            int numberRows, int numberColumns, File file) throws IOException, ProjectException;
    }
    private static class ImportLXFMLProject extends ImportProjectFactory {

        @Override
        public Project create(
                ProjectManager manager, String name, Path path, 
                int numberRows, int numberColumns, File file) throws IOException, ProjectException {
            FileInputStream stream = new FileInputStream(file);
            
            LegoAssembly assembly = LXFMLReader.createAssembly(stream, numberRows, numberColumns);
            ProjectScene scene = ProjectScene.createSceneFrom(name, assembly);
            
            return manager.createProject(path, name, numberRows, numberColumns, scene);
        }

    }

    private static class ImportSTLProject extends ImportProjectFactory {

        @Override
        public Project create(
                ProjectManager manager, String name, Path path, 
                int numberRows, int numberColumns, File file) throws IOException, ProjectException {            
            LegoAssembly assembly = Slicer.pipeline(file, numberRows, numberColumns);
            ProjectScene scene = ProjectScene.createSceneFrom(name, assembly);
            
            return manager.createProject(path, name, numberRows, numberColumns, scene);
        }

    }

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

        if (fileField.getFile() == null) {
            setError("Please select a file.");
            return false;
        }

        if (factoryProperty.get() == null) {
            setError("Unrecognized extension.");
            return false;
        }

        try {
            factoryProperty.get()
                .create(
                    manager,
                    name,
                    resolved,
                    numberRows,
                    numberColumns, 
                    fileField.getFile());
        } catch (ProjectException e) {
            e.printStackTrace();
            setError("Unexpected error");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            setError("Unexpected IO Exception");
            return false;
        }

        return true;
    }

    private static final StringProperty titleProperty = new SimpleStringProperty("New Project");

    private TextField projectName;
    private TextField projectPath;

    private IntegerTextField plateNumberRows;
    private IntegerTextField plateNumberColumns;

    private FileField fileField;

    private ObjectProperty<ImportProjectFactory> factoryProperty;

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

        fileField = new FileField(stage, "Choose LEGO file...");
        fileField.setMaxWidth(Double.MAX_VALUE);

        HBox fileFieldBox = new HBox(fileField);
        HBox.setHgrow(fileField, Priority.ALWAYS);

        factoryProperty = new SimpleObjectProperty<ImportProjectForm.ImportProjectFactory>();
        factoryProperty.bind( Bindings.createObjectBinding(
            () -> {
                if (fileField.fileProperty().get() == null) return null;

                String extension = FilenameUtils.getExtension(
                    fileField.fileProperty().get().getName()
                );

                return ImportProjectFactory.getFactoryForFile(extension);
            },
            fileField.fileProperty()
        ) );

        rendered = new VBox(
            projectName, 
            projectPath,
            plateNumberRows,
            plateNumberColumns,
            fileFieldBox);
        
        return rendered;
    }
    
}
