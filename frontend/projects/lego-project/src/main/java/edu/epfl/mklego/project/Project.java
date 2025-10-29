package edu.epfl.mklego.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Project {

    public static final String PROJECT_FILE_LOCATION = "project.mkl";

    private final Path path;

    private final StringProperty name;
    private final ObjectProperty<LocalDateTime> lastModified;

    private final BooleanProperty modified = new SimpleBooleanProperty(false);

    private ObjectMapper mapper;
    
    @JsonIgnore
    private void setObjectMapper (ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @JsonCreator
    private Project (
            @JacksonInject("projectPath") Path path, 
    
            @JsonProperty("name")         String name,
            @JsonProperty("lastModified") LocalDateTime lastModified) {
        this.path = path;

        this.name = new SimpleStringProperty(name);
        this.lastModified = new SimpleObjectProperty<LocalDateTime>(lastModified);

        this.name.addListener((obs, old, nwv) -> modified.set(true));
        this.lastModified.addListener((obs, old, nwv) -> modified.set(true));
    }

    public String getName () {
        return name.get();
    }
    public LocalDateTime getLastModified () {
        return lastModified.get();
    }
    
    public void setName (String name) {
        this.name.set(name);
    }
    public void setLastModified (LocalDateTime lastModified) {
        this.lastModified.set(lastModified);
    }

    public StringProperty nameProperty () {
        return name;
    }
    public ObjectProperty<LocalDateTime> lastModifiedProperty () {
        return lastModified;
    }

    @JsonIgnore
    public boolean isModified () {
        return modified.get();
    }
    @JsonIgnore
    public ReadOnlyBooleanProperty modifiedProperty () {
        ReadOnlyBooleanWrapper wrapper = new ReadOnlyBooleanWrapper();
        wrapper.bind( modified );

        return wrapper;
    }

    public void save () throws ProjectException {
        save(false);
    }
    public void save (boolean forced) throws ProjectException {
        if (!this.isModified() && !forced) return ;

        try {
            Files.createDirectories(path);
        } catch (IOException exception) {
            throw new ProjectException("Could not create directory '" + path + "'");
        }

        Path projectPath = path.resolve(PROJECT_FILE_LOCATION);
        File projectFile = projectPath.toFile();
        
        setLastModified(LocalDateTime.now());
        
        try {
            mapper.writeValue(projectFile, this);
        } catch (IOException exception) {
            if (forced)
                throw new ProjectException("Could not create project file '" + projectPath + "'");
            else
                throw new ProjectException("Could not save project file '" + projectPath + "'");
        }

        modified.set(false);
    }

    public static Project readFromPath (ObjectMapper mapper, Path path) throws ProjectException {
        Path projectPath = path.resolve(PROJECT_FILE_LOCATION);
        File projectFile = projectPath.toFile();
        
        InjectableValues inject = new InjectableValues.Std()
            .addValue("projectPath", path);
        
        try {
            Project project = mapper.reader()
                .forType(Project.class)
                .with(inject)
                .readValue(projectFile);
            project.setObjectMapper(mapper);
            return project;
        } catch (IOException e) {
            throw new ProjectException("Could not read project at path '" + path + "' (" + e.getMessage() + ")");
        }
    }
    public static Project createProject (ObjectMapper mapper, Path path, String name) throws ProjectException {
        Project project = new Project(path, name, LocalDateTime.now());
        project.setObjectMapper(mapper);
        
        if (Files.exists(path))
            throw new ProjectException ("Project at the path '" + path + "' already exists");
        
        project.save(true);

        return project;
    }

    public static void main(String[] args) throws ProjectException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        Path path = Path.of("./mklego-save-projects");
        Project project = Project.createProject(mapper, path, "Project name");
        
        try {
            String value = (mapper.writeValueAsString(project));
            System.out.println("=== VALUE ===");
            System.out.println(value);
            System.out.println();

            Project proj = readFromPath(mapper, path);
            
            System.out.println("=== READ RESULT ===");
            System.out.println(proj.path);
            System.out.println(proj.name);
            System.out.println(proj.lastModified);
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
