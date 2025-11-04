package edu.epfl.mklego.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.epfl.mklego.project.json.ObjectMapperConfig;
import edu.epfl.mklego.project.scene.Entity;
import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.Transform;
import edu.epfl.mklego.project.scene.Transform.Observable3f;
import edu.epfl.mklego.project.scene.entities.GroupEntity;
import edu.epfl.mklego.project.scene.entities.LegoPieceEntity;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class Project {

    public static final String PROJECT_FILE_LOCATION = "project.mkl";

    private final Path path;

    private final StringProperty name;
    private final ObjectProperty<LocalDateTime> lastModified;
    
    private final ProjectScene scene;

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
            @JsonProperty("lastModified") LocalDateTime lastModified,
            @JsonProperty("scene")        ProjectScene scene) {
        this.path = path;

        this.name = new SimpleStringProperty(name);
        this.lastModified = new SimpleObjectProperty<LocalDateTime>(lastModified);

        this.name.addListener((obs, old, nwv) -> modified.set(true));
        this.lastModified.addListener((obs, old, nwv) -> modified.set(true));

        this.scene = scene;
        this.scene.modifiedProperty()
            .addListener((obs, old, nwv) -> {
                if (nwv) modified.set(true);
            });
    }

    public ProjectScene getScene () {
        return scene;
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
    public static Project createProject (
            ObjectMapper mapper, Path path, String name,
            int plateNumberRows, int plateNumberColumns) throws ProjectException {
        ProjectScene scene = ProjectScene.createEmptyScene(name, plateNumberRows, plateNumberColumns);
        Project project = new Project(path, name, LocalDateTime.now(), scene);
        project.setObjectMapper(mapper);
        
        if (Files.exists(path))
            throw new ProjectException ("Project at the path '" + path + "' already exists");
        
        project.save(true);

        return project;
    }

    public static void _assert (boolean b) {
        if (! b) throw new RuntimeException();
    }
    public static void main(String[] args) throws ProjectException, JsonProcessingException {
        /*List<Modifiable> mods = new ArrayList<>();
        for (int i = 0; i < 3; i ++) {
            mods.add(new Modifiable() {
                private final BooleanProperty prop = new SimpleBooleanProperty(false);

                @Override
                public boolean isModified() { return prop.get(); }

                @Override
                public BooleanProperty modifiedProperty() { return prop; }

                @Override
                public void save() {
                    prop.set(false);
                }
            });
        }

        List<Modifiable> vmods = new ArrayList<>();
        vmods.add(mods.get(0));

        ObservableList<Modifiable> props = FXCollections.observableList(vmods);

        SimpleListWatcher watcher = new SimpleListWatcher( props );

        watcher.modifiedProperty()
            .addListener((obs, old, nwv) -> System.out.println(nwv));

        System.out.println("===");
        mods.get(0).modifiedProperty().set(true);
        System.out.println("===");
        watcher.save();
        System.out.println("===");
        props.add( mods.get(1) );
        System.out.println("===");
        watcher.save();
        System.out.println("===");
        props.add( mods.get(2) );
        System.out.println("===");
        watcher.save();
        System.out.println("=== MODS ===");
        props.remove( mods.get(1) );
        System.out.println("===");
        watcher.save();
        System.out.println("===");
        mods.get(1).modifiedProperty().set(true);
        System.out.println("===");*/

        ObjectMapper mapper = ObjectMapperConfig.configureMapper();
        
        //Path path = Path.of("./mklego-save-projects");
        Transform trns = new Transform(
            new Observable3f(2.0f, 0.5f, 1.2f),
            new Observable3f(3.0f, 1.5f, 2.2f),
            new Observable3f(1.0f, -0.5f, 0.2f)
        );
        Color color = Color.CORNFLOWERBLUE;
        Entity ent = new LegoPieceEntity(trns, "lego", color, 2, 4);
        
        Entity gnt = new GroupEntity(new Transform(), "group", List.of(ent, ent));

        trns.getTranslation().setX(0.0f);
        _assert(!trns.getScale().isModified());
        _assert(!trns.getRotation().isModified());
        _assert(trns.getTranslation().isModified());
        _assert(trns.isModified());
        _assert(ent.isModified());
        _assert(gnt.isModified());
        gnt.save();
        _assert(!trns.getScale().isModified());
        _assert(!trns.getRotation().isModified());
        _assert(!trns.getTranslation().isModified());
        _assert(!trns.isModified());
        _assert(!ent.isModified());
        _assert(!gnt.isModified());

        try {
            String value = (mapper.writeValueAsString(gnt));
            System.out.println("=== VALUE ===");
            System.out.println(value);
            System.out.println();

            Entity ent2 = mapper.reader().forType(Entity.class).readValue(value);
            //Project proj = readFromPath(mapper, path);
            
            System.out.println("=== READ RESULT ===");
            System.out.println("CLASS: " + ent2.getClass());
            System.out.println();
            System.out.println("COLOR: " + ((LegoPieceEntity) ent2).getColor());
            System.out.println();
            System.out.println("SCALE: " + ent2.getTransform().getScale().getX() + " " + ent2.getTransform().getScale().getY() + " " + ent2.getTransform().getScale().getZ());
            System.out.println("TRANS: " + ent2.getTransform().getTranslation().getX() + " " + ent2.getTransform().getTranslation().getY() + " " + ent2.getTransform().getTranslation().getZ());
            System.out.println("ROTAT: " + ent2.getTransform().getRotation().getX() + " " + ent2.getTransform().getRotation().getY() + " " + ent2.getTransform().getRotation().getZ());
            System.out.println();
            System.out.println("NROWS: " + ((LegoPieceEntity) ent2).getNumberRows());
            System.out.println("NCOLS: " + ((LegoPieceEntity) ent2).getNumberColumns());
        } catch (JsonProcessingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
