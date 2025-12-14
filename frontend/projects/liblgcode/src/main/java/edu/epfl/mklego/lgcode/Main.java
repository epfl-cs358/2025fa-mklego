package edu.epfl.mklego.lgcode;

import java.io.IOException;

import org.apache.commons.cli.ParseException;

import edu.epfl.mklego.lgcode.cli.Command;
import edu.epfl.mklego.lgcode.cli.CommandFactory;

public class Main {
    
    public static void main (String[] args) throws ParseException, IOException, edu.epfl.mklego.lgcode.format.ParseException {
        Command command = CommandFactory.findCommand(args[0]);
        
        String[] commandArgs = new String[args.length - 1];
        for (int idx = 1; idx < args.length; idx ++)
            commandArgs[idx - 1] = args[idx];
        
        command.execute(commandArgs);
    }
}