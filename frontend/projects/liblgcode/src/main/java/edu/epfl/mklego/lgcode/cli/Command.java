package edu.epfl.mklego.lgcode.cli;

import java.io.IOException;

@FunctionalInterface
public interface Command {
    public void execute (String[] args) throws IOException, edu.epfl.mklego.lgcode.format.ParseException;
}
