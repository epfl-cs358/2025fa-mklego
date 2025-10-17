package edu.epfl.mklego.lgcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExceptionGroup extends Exception {
    private final List<Exception> exceptions = new ArrayList<Exception>();

    public void addException (Exception exception) {
        this.exceptions.add(exception);
    }
    public List<Exception> exceptions () {
        return Collections.unmodifiableList(exceptions);
    }
    public void tryThrow () throws ExceptionGroup {
        if (this.exceptions.size() > 0) throw this;
    }
}
