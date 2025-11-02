package edu.epfl.mklego.project.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperConfig {
    public static ObjectMapper configureMapper () {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        mapper.registerModule(new FxColorModule());
        return mapper;
    }
}
