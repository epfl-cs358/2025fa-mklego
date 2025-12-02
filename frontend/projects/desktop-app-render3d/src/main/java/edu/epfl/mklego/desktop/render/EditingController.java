package edu.epfl.mklego.desktop.render;

import edu.epfl.mklego.project.Project;
import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.entities.LegoPiece;
import edu.epfl.mklego.desktop.render.EditingController.Ray;
import edu.epfl.mklego.desktop.render.mesh.LegoPieceMesh;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.MouseEvent;

import java.sql.PreparedStatement;
import java.util.List;

/**
 * Controller responsible for selecting, adding, moving, and deleting LEGO pieces
 * using 3D ray picking.
 */
public class EditingController extends SceneController {

    private Scene3D scene;

    // Interaction state --------------------------------------------

    private LegoPiece selectedPiece = null;
    private LegoPieceMesh selectedMesh = null;

    public enum Mode {
        SELECT,
        MOVE,
        DELETE,
        ADD
    }

    private Mode currentMode = Mode.SELECT;

    public EditingController() {
        super(true);
    }

    // SceneController hooks --------------------------------------------

    @Override
    public void control(Scene3D scene) {
        this.scene = scene;

        // Attach mouse listeners
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    @Override
    public void dispose(Scene3D scene) {
        // Remove listeners to avoid memory leaks
        scene.removeEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        scene.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        scene.removeEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    // Mouse handlers --------------------------------------------

    private void handleMousePressed(MouseEvent e) {
        if (!isEnabled())
            return;

        PickResult pick = pickLegoPiece(scene, e.getScreenX(), e.getScreenY());
        if (pick == null) {
            clearSelection();
            return;
        }

        switch (currentMode) {
            case SELECT -> setSelection(pick);
            case DELETE -> deletePiece(pick);
            case MOVE -> beginMove(pick, e);
            case ADD -> addPieceOnFace(pick);
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (!isEnabled())
            return;

        if (currentMode == Mode.MOVE && selectedPiece != null) {
            continueMove(e);
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        if (!isEnabled())
            return;

        if (currentMode == Mode.MOVE && selectedPiece != null) {
            endMove(e);
        }
    }

    // Selection logic --------------------------------------------

    private void setSelection(PickResult pick) {
        clearSelection();
        this.selectedPiece = pick.piece;
        this.selectedMesh = pick.mesh;
        scene.highlightMesh(selectedMesh, true);
    }

    private void clearSelection() {
        if (selectedMesh != null)
            scene.highlightMesh(selectedMesh, false);

        selectedPiece = null;
        selectedMesh = null;
    }

    // Move logic --------------------------------------------

    private void beginMove(PickResult pick, MouseEvent e) {
        // TODO: store initial drag offset, initial piece coordinate, etc.
    }

    private void continueMove(MouseEvent e) {
        // TODO: convert mouse drag -> 3D movement -> snap to grid
    }

    private void endMove(MouseEvent e) {
        // TODO: finalize update and update LegoAssembly
    }

    // Delete logic --------------------------------------------

    private void deletePiece(PickResult pick) {
        // TODO: remove piece from assembly and rerender
        ProjectScene sceneData = scene.getProjectScene();
        List<LegoPiece> pieces = sceneData.getLegoAssembly().getPieces();
        pieces.remove(pick.piece);
        // Rerender scene after deletion
        Node sceneNode = new SceneRenderer().render(sceneData);
        Group root3D = (Group) scene.getRoot();
        root3D.getChildren().clear();
        root3D.getChildren().add(sceneNode);
    }

    // Add piece logic --------------------------------------------

    private void addPieceOnFace(PickResult pick) {
        // TODO: use pick.faceNormal to place a new piece adjacent to the face
    }

    // Picking pipeline --------------------------------------------

    //Returns the LEGO piece that the ray hits, if any.
    protected PickResult pickLegoPiece(Scene3D scene, double screenX, double screenY) {
        Ray pickRay = computePickRay(scene, screenX, screenY);
        if (pickRay == null)
            return null;

        List<LegoPiece> pieces = scene.getProjectScene().getLegoAssembly().getPieces();
        List<LegoPieceMesh > legoMeshes = List.of();
        //List<LegoPieceMesh> legoMeshes = scene.getAllPieceMeshes(); // TODO: get all LegoPieceMesh objects in the scene

        PickResult nearestPick = null;
        double nearestDist = Double.POSITIVE_INFINITY;

        for (LegoPieceMesh mesh : legoMeshes) {
            PickResult hit = intersectWithPiece(pickRay.origin, pickRay.direction, mesh);
            if (hit != null) {
                double dist = hit.originToHitDistance(pickRay.origin);
                if (dist < nearestDist){
                    nearestDist = dist;
                    nearestPick = hit;
                }
            }

        }
        return nearestPick;
    }

    //Intersects the pick ray with one LEGO mesh.
    protected PickResult intersectWithPiece(
            Point3D rayOrigin,
            Point3D rayDir,
            LegoPieceMesh mesh
    ) {
        // TODO: implement Möller–Trumbore on mesh triangles
        return null;
    }

    //Computes the pick ray originating from the camera through the screen pixel.
    protected Ray computePickRay(Scene3D scene, double screenX, double screenY) {
        Camera camera = scene.getCamera();

        Point2D local2D = scene.screenToLocal(screenX, screenY);
        if (local2D == null) return null;

        Point3D nearPoint = new Point3D(local2D.getX(), local2D.getY(), 0);

        // another point on the far clip plane
        Point3D farPoint  = new Point3D(local2D.getX(), local2D.getY(), 1);

        Point3D nearInWorld = camera.localToScene(nearPoint);
        Point3D farInWorld  = camera.localToScene(farPoint);

        Point3D origin = nearInWorld;
        Point3D direction = farInWorld.subtract(nearInWorld).normalize();

        return new Ray(origin, direction);
    }

    // Data structure to hold picking results --------------------------------------------

    protected static class PickResult {
        public final LegoPiece piece;
        public final LegoPieceMesh mesh;
        public final Point3D hitPoint;
        public final Point3D faceNormal;

        public PickResult(LegoPiece piece, LegoPieceMesh mesh,
                          Point3D hitPoint, Point3D faceNormal) {
            this.piece = piece;
            this.mesh = mesh;
            this.hitPoint = hitPoint;
            this.faceNormal = faceNormal;
        }

        private double originToHitDistance(Point3D rayOrigin) {
            return hitPoint.distance(rayOrigin);
        }
    }

    // Ray structure --------------------------------------------

    protected static class Ray {
        public final Point3D origin;
        public final Point3D direction;

        public Ray(Point3D origin, Point3D direction) {
            this.origin = origin;
            this.direction = direction.normalize();
        }
    }

    // Mode switching API --------------------------------------------

    public void setMode(Mode mode) {
        this.currentMode = mode;
    }

    public Mode getMode() {
        return currentMode;
    }

    // Piece API --------------------------------------------
    public List<LegoPieceMesh> pieceToMesh(LegoPiece piece) {
        //LegoPieceMesh.createPiece(piece.getNumberColumns(), piece.getNumberRows());
        return null;
        // TODO
    }

}



