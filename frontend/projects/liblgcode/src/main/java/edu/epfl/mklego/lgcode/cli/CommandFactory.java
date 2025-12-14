package edu.epfl.mklego.lgcode.cli;

public class CommandFactory {
    
    public static void version () {
        System.out.println("LibLGCode 1.0.0 - CLI 1.0.0");
        System.out.println("MKLego Software under MIT License");
    }
    public static void help () {
        System.out.println("LibLGCode 1.0.0 - CLI 1.0.0 / Help message");
        System.out.println("");
        System.out.println("  --version, -v, version  Display the version");
        System.out.println("  --help, -h, help        Display this help");
        System.out.println("  dump                    Display a lgcode file as text lgcode");
        System.out.println("  convert                 Convert an lgcode file to another type");
        System.out.println("");
    }

    public static Command findCommand (String arg) {
        switch (arg) {
            case "dump":
                return new DumpCommand();
            case "convert":
                return new ConvertCommand();
            case "version":
            case "--version":
            case "-v":
                return (args) -> version();
            case "--help":
            case "help":
            case "-h":
                return (args) -> help();
            default:
                System.out.println("Unrecognized command: " + arg);
                return (args) -> help();
        }
    }

}
