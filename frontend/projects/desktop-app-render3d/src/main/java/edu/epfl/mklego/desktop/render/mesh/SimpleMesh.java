package edu.epfl.mklego.desktop.render.mesh;

import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;

public class SimpleMesh extends MeshView {
    private final PhongMaterial material;

    public SimpleMesh (Mesh mesh, Color color) {
        super(mesh);
        
        this.material = new PhongMaterial();
        setMaterial(material);

        this.material.setDiffuseColor(color);
    }
    public SimpleMesh (Mesh mesh) {
        this(mesh, Color.WHITE);
    }
}
