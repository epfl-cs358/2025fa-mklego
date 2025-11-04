package edu.epfl.mklego.desktop.utils.form;

import javafx.scene.layout.BorderPane;

public class ModalFormContainer extends BorderPane {
    
    private ModalForm form = null;

    private ModalFormContainer () {
        super();

        this.setPickOnBounds(false);
    }
    private static ModalFormContainer singleton = null;
    public static ModalFormContainer getInstance () {
        if (singleton == null)
            singleton = new ModalFormContainer();
        return singleton;
    }

    public ModalForm getForm () {
        return form;
    }
    public void setForm (ModalForm form) {
        this.form = form;
        this.setCenter(form);
    }

}
