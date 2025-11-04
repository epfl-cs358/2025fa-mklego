package edu.epfl.mklego.project;

public class ProjectException extends Exception {
    
    public final String source = "Project";

    public ProjectException (String message) {
        super(message);
    }
}
