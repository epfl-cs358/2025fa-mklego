package edu.epfl.mklego.lgcode.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.epfl.mklego.lgcode.Constants;
import edu.epfl.mklego.lgcode.LGCode;

public class ConvertCommand implements edu.epfl.mklego.lgcode.cli.Command {

    private Options getOptions () {
        Options options = new Options();

        OptionGroup optgrp = new OptionGroup();
        optgrp.addOption(
            Option.builder("b")
                .longOpt("binary")
                .desc("Use binary LG-CODE")
                .get()
        );
        optgrp.addOption(
            Option.builder("t")
                .longOpt("text")
                .desc("Use text LG-CODE")
                .get()
        );

        options.addOptionGroup(optgrp);

        return options;
    }
    private CommandLine parse (String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        
        return parser.parse(getOptions(), args);
    }
    @SuppressWarnings("deprecation")
    private void displayHelp () {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(Constants.LGCODE_LIB_NAME + " convert <input> <output>", getOptions());
    }

    @Override
    public void execute(String[] args) throws IOException, edu.epfl.mklego.lgcode.format.ParseException {
        CommandLine commandLine = null;
        
        try {
            commandLine = parse(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            displayHelp();
            return ;
        }

        String[] posArgs = commandLine.getArgs();
        if (posArgs.length < 2) {
            System.out.println("Missing positional arguments.");
            displayHelp();
            return ;
        }
        if (posArgs.length > 2) {
            System.out.println("Too many positional arguments.");
            displayHelp();
            return ;
        }

        String filePath = posArgs[0];
        
        File file = new File(filePath);
        InputStream stream = new FileInputStream(file);

        LGCode code = LGCode.read(stream);

        File outputFile = new File(posArgs[1]);
    
        OutputStream outputStream = new FileOutputStream(outputFile);
        if (commandLine.hasOption("b")) code.writeBinary(outputStream);
        else if (commandLine.hasOption("t")) code.writeText(outputStream);
    }
    
}
