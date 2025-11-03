package edu.epfl.mklego.project.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.epfl.mklego.project.json.watcher.Modifiable;
import edu.epfl.mklego.project.json.watcher.SimplePropertyWatcher;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;

public class Transform implements Modifiable {
    public static class Observable3f implements Modifiable {
        private final FloatProperty x, y, z;

        public float getX () {
            return x.get();
        }
        public float getY () {
            return y.get();
        }
        public float getZ () {
            return z.get();
        }

        public void setX (float x) {
            this.x.set(x);
        }
        public void setY (float y) {
            this.y.set(y);
        }
        public void setZ (float z) {
            this.z.set(z);
        }

        public FloatProperty xProperty () {
            return x;
        }
        public FloatProperty yProperty () {
            return y;
        }
        public FloatProperty zProperty () {
            return z;
        }
     
        public Observable3f () {
            this(0, 0, 0);
        }
        public Observable3f (float x, float y, float z) {
            this.x = new SimpleFloatProperty(x);
            this.y = new SimpleFloatProperty(y);
            this.z = new SimpleFloatProperty(z);

            watcher = new SimplePropertyWatcher()
                .withProperty(this.x)
                .withProperty(this.y)
                .withProperty(this.z);
            isModified = watcher.build();
        }

        private final SimplePropertyWatcher watcher;
        private final BooleanProperty isModified;

        @JsonIgnore
        @Override
        public boolean isModified() {
            return isModified.get();
        }
        @JsonIgnore
        @Override
        public BooleanProperty modifiedProperty() {
            return isModified;
        }
        @Override
        public void save() {
            watcher.save();
        }
    }

    private final Observable3f scale;
    private final Observable3f translation;
    private final Observable3f rotation;

    public Observable3f getScale () {
        return scale;
    }
    public Observable3f getTranslation () {
        return translation;
    }
    public Observable3f getRotation () {
        return rotation;
    }

    public Transform () {
        this(
            new Observable3f(1, 1, 1),
            new Observable3f(0, 0, 0),
            new Observable3f(0, 0, 0)
        );
    }
    public Transform (Observable3f scale, Observable3f translation, Observable3f rotation) {
        this.scale       = scale;
        this.translation = translation;
        this.rotation    = rotation;
        
        watcher = new SimplePropertyWatcher()
            .withModifiable(this.scale)
            .withModifiable(this.translation)
            .withModifiable(this.rotation);
        isModified = watcher.build();
    }

    private final SimplePropertyWatcher watcher;
    private final BooleanProperty isModified;
    
    @JsonIgnore
    @Override
    public boolean isModified() {
        return isModified.get();
    }
    @JsonIgnore
    @Override
    public BooleanProperty modifiedProperty() {
        return isModified;
    }
    @Override
    public void save() {
        watcher.save();
    }
}
