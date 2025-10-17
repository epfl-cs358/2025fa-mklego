package edu.epfl.mklego.lgcode;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Main {
    private static Options optionsSingleton = null;
    public static Options getOptions () {
        if (optionsSingleton != null) return optionsSingleton;

        Options options = new Options();
        options.addOption("v", "version", false, "Displays the version of the liblgcode cli.");
    
        optionsSingleton = options;

        return options;
    }
    public static CommandLine parse (String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        
        return parser.parse(getOptions(), args);
    }
    @SuppressWarnings("deprecation")
    public static void displayHelp () {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(Constants.LGCODE_LIB_NAME, getOptions());
    }

    public static void main (String[] args) {
        CommandLine command = null;

        try {
            command = parse(args);
        } catch (ParseException exception) {
            System.err.println("Error parsing command line: " + exception.getMessage());
            
            displayHelp();
            System.exit(1);
        }

        if (command.hasOption("version")) {
            System.out.println("liblgcode version 1.0.0");
            return ;
        }
    }
}