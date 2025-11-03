package edu.epfl.mklego.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.epfl.mklego.project.json.ObjectMapperConfig;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class ProjectManager {
    
    private final ObjectMapper mapper = ObjectMapperConfig.configureMapper();

    private final ListProperty<Project> projects;
    public ListProperty<Project> projectsProperty () {
        return projects;
    }

    public ProjectManager (Path rootPath) throws ProjectException {
        if (Files.exists(rootPath) && !Files.isDirectory(rootPath)) {
            throw new ProjectException("The given root path '" + rootPath.toAbsolutePath() + "' already exists and isn't a directory.");
        }
        if (!Files.exists(rootPath)) {
            try {
                Files.createDirectories(rootPath);
            } catch (IOException e) {
                throw new ProjectException("Could not create the root directory '" + rootPath.toAbsolutePath() + "'");
            }
        }

        List<Path> subpaths;
        try {
            subpaths = Files.list(rootPath)
                .filter(path -> Files.isDirectory(path))
                .toList();
        } catch (IOException e) {
            throw new ProjectException("Could not list the files in the root directory '" + rootPath.toAbsolutePath() + "'");
        }

        for (Path p : subpaths) System.out.println("Subpath: " + p);
        
        List<Project> projects = new ArrayList<>();
        for (Path path : subpaths)
            projects.add( Project.readFromPath(mapper, path) );
        
        this.projects = new SimpleListProperty<>( FXCollections.observableArrayList(projects) );
    }

    public Project createProject (Path path, String name, int plateNumberRows, int plateNumberColumns) throws ProjectException {
        Project project = Project.createProject(mapper, path, name, plateNumberRows, plateNumberColumns);
        projects.add(project);

        return project;
    }

}
