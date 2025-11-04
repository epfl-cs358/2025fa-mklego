package edu.epfl.mklego.project.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javafx.scene.paint.Color;

public class FxColorModule extends SimpleModule {

    private class FxColorSerializer extends StdSerializer<Color> {

        public FxColorSerializer () {
            this(null);
        }
        public FxColorSerializer (Class<Color> t) {
            super(t);
        }
        
        @Override
        public void serialize(Color value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }

    }

    private class FxColorDeserializer extends StdDeserializer<Color> {

        public FxColorDeserializer () {
            this(null);
        }
        public FxColorDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Color deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
            return Color.web( p.getText() );
        }

    }

    public FxColorModule () {
        super("FxColorModule");

        addSerializer  (Color.class, new FxColorSerializer());
        addDeserializer(Color.class, new FxColorDeserializer());
    }
}
