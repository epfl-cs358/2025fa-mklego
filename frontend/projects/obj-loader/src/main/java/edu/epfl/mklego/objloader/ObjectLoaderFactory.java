package edu.epfl.mklego.objloader;

import org.apache.commons.io.FilenameUtils;

import edu.epfl.mklego.objloader.impl.STLObjectLoader;

public class ObjectLoaderFactory {
    public static class UnknownObjectFileFormatException extends Exception {
        public UnknownObjectFileFormatException (String message) {
            super(message);
        }
    }

    public static ObjectLoader getObjectLoader (String fileName)
            throws UnknownObjectFileFormatException {
        String extension = FilenameUtils.getExtension(fileName);

        switch (extension) {
            case "stl":
                return new STLObjectLoader();
        
            default:
                break;
        }
        
        throw new UnknownObjectFileFormatException("Could not find ObjectLoader for extension " + extension);
    }
}
