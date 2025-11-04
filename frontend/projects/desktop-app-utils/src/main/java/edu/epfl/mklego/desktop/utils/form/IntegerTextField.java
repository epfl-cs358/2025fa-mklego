package edu.epfl.mklego.desktop.utils.form;

import javafx.scene.control.TextField;

public class IntegerTextField extends TextField {
    
    public IntegerTextField (int value) {
        this();

        setText(String.valueOf(value));
    }
    public IntegerTextField () {
        this.textProperty().addListener(
            (obs, old, nwv) -> {
                if (!nwv.matches("\\d*")) {
                    this.setText(nwv.replaceAll("[^\\d]", ""));
                } else if (nwv.startsWith("0") && nwv.length() != 1) {
                    setText(nwv.substring(1));
                }
            });
    }

    public boolean hasValue () {
        return !this.getText().equals("");
    }
    public int getValue () {
        return Integer.parseInt(this.textProperty().get());
    }
    public void setValue (int value) {
        setText(Integer.toString(value));
    }

}
