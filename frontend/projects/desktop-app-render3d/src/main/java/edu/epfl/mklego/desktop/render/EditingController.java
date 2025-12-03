package edu.epfl.mklego.desktop.render;

import edu.epfl.mklego.project.Project;
import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import edu.epfl.mklego.project.scene.entities.LegoPiece;
import edu.epfl.mklego.desktop.render.EditingController.Ray;
import edu.epfl.mklego.desktop.render.mesh.LegoMeshView;
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

    // Interaction state =======================================================

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

    // SceneController hooks ======================================================

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

    // Mouse handlers ============================================================

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

    // Selection logic =========================================================

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

    // Move logic ============================================================

    private void beginMove(PickResult pick, MouseEvent e) {
        // TODO: store initial drag offset, initial piece coordinate, etc.
    }

    private void continueMove(MouseEvent e) {
        // TODO: convert mouse drag -> 3D movement -> snap to grid
    }

    private void endMove(MouseEvent e) {
        // TODO: finalize update and update LegoAssembly
    }

    // Delete logic =============================================================

    private void deletePiece(PickResult pick) {
        if (pick == null || scene == null) {
            return;
        }

        ProjectScene sceneData = scene.getProjectScene();
        if (sceneData == null || sceneData.getLegoAssembly() == null) {
            return;
        }

        List<LegoPiece> pieces = sceneData.getLegoAssembly().getPieces();
        boolean removed = pieces.remove(pick.piece);
        if (!removed) {
            return; // nothing to do
        }

        if (pick.piece == selectedPiece) {
            clearSelection();
        }

        Group root3D = (Group) scene.getRoot();
        if (root3D.getChildren().isEmpty()) {
            return;
        }

        Node newSceneNode = new SceneRenderer().render(sceneData);

        // assuming index 0 is always the main rendered scene node
        root3D.getChildren().set(0, newSceneNode);
    }


    // Add piece logic ===========================================================

    private void addPieceOnFace(PickResult pick) {
        if (pick == null || scene == null)
            return;

        ProjectScene sceneData = scene.getProjectScene();
        LegoAssembly assembly = sceneData.getLegoAssembly();

        LegoPiece clicked = pick.piece;

        Point3D normal = pick.faceNormal.normalize();

        int dRow = 0;
        int dCol = 0;
        int dHeight = 0;

        // interpret face normal in LEGO grid units
        // adjust threshold if needed based on mesh orientation
        if (Math.abs(normal.getX()) > Math.abs(normal.getY()) &&
            Math.abs(normal.getX()) > Math.abs(normal.getZ())) {
            // X dominant => left / right
            dRow = (normal.getX() > 0) ? 1 : -1;
        } 
        else if (Math.abs(normal.getY()) > Math.abs(normal.getX()) &&
                Math.abs(normal.getY()) > Math.abs(normal.getZ())) {
            // Y domainant => front / back
            dCol = (normal.getY() > 0) ? 1 : -1;
        }
        else {
            // Z dominant => vertical stacking
            dHeight = (normal.getZ() > 0) ? 1 : -1;
        }

        // New piece coordinates
        int newRow    = clicked.getMainStubRow()    + dRow;
        int newCol    = clicked.getMainStubCol()    + dCol;
        int newHeight = clicked.getMainStubHeight() + dHeight;

        // Create a new piece of same kind and same color
        LegoPiece newPiece = new LegoPiece(
            newRow,
            newCol,
            newHeight,
            clicked.getColor(),
            clicked.getKind()
        );

        assembly.getPieces().add(newPiece);

        Group root3D = (Group) scene.getRoot();
        if (!root3D.getChildren().isEmpty()) {
            Node newSceneNode = new SceneRenderer().render(sceneData);
            root3D.getChildren().set(0, newSceneNode);
        }
    }


    // Picking pipeline ==============================================================

    //Returns the LEGO piece that the ray hits, if any.
        protected PickResult pickLegoPiece(Scene3D scene, double screenX, double screenY) {
        Ray pickRay = computePickRay(scene, screenX, screenY);
        if (pickRay == null)
            return null;

        List<LegoMeshView> pieceViews = scene.getAllPieceViews();

        PickResult nearest = null;
        double nearestDist = Double.POSITIVE_INFINITY;

        for (LegoMeshView view : pieceViews) {

            // mesh in local coordinates
            LegoPieceMesh mesh = (LegoPieceMesh) view.getMesh();
            if (mesh == null)
                continue;

            // transform ray into the mesh’s local coordinate system
            Point3D localOrigin = view.sceneToLocal(pickRay.origin);
            Point3D localEnd    = view.sceneToLocal(pickRay.origin.add(pickRay.direction));
            Point3D localDir    = localEnd.subtract(localOrigin).normalize();

            PickResult hit = intersectWithPiece(localOrigin, localDir, mesh, view);

            if (hit != null) {
                double dist = hit.originToHitDistance(pickRay.origin);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = hit;
                }
            }
        }

        return nearest;
    }


    // Intersects the pick ray with one LEGO mesh using Möller–Trumbore algorithm.
    protected PickResult intersectWithPiece(
            Point3D rayOriginLocal,
            Point3D rayDirLocal,
            LegoPieceMesh mesh,
            LegoMeshView view)
    {
        float[] pts = mesh.getPoints().toArray(null);
        int[] faces = mesh.getFaces().toArray(null);
        int stride = mesh.getFaceElementSize();       // should be 2 (point index, tex index)
        
        Point3D bestHit = null;
        Point3D bestNormal = null;
        double nearestDist = Double.POSITIVE_INFINITY;

        for (int i = 0; i < faces.length; i += 3 * stride) {
            
            int p0Index = faces[i]     * 3;
            int p1Index = faces[i+2]   * 3;
            int p2Index = faces[i+4]   * 3;

            Point3D v0 = new Point3D(pts[p0Index], pts[p0Index+1], pts[p0Index+2]);
            Point3D v1 = new Point3D(pts[p1Index], pts[p1Index+1], pts[p1Index+2]);
            Point3D v2 = new Point3D(pts[p2Index], pts[p2Index+1], pts[p2Index+2]);

            // ---- Möller–Trumbore ----
            Point3D edge1 = v1.subtract(v0);
            Point3D edge2 = v2.subtract(v0);

            Point3D h = rayDirLocal.crossProduct(edge2);
            double a = edge1.dotProduct(h);

            if (Math.abs(a) < 1e-7)
                continue; // Ray is parallel to triangle

            double f = 1.0 / a;
            Point3D s = rayOriginLocal.subtract(v0);
            double u = f * s.dotProduct(h);

            if (u < 0.0 || u > 1.0)
                continue;

            Point3D q = s.crossProduct(edge1);
            double v = f * rayDirLocal.dotProduct(q);

            if (v < 0.0 || u + v > 1.0)
                continue;

            double t = f * edge2.dotProduct(q);
            if (t > 1e-7) {
                // Intersection at rayOriginLocal + t*rayDirLocal
                Point3D hitLocal = rayOriginLocal.add(rayDirLocal.multiply(t));

                double dist = hitLocal.distance(rayOriginLocal);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    bestHit = hitLocal;

                    // Compute normal in LOCAL space
                    Point3D normalLocal = edge1.crossProduct(edge2).normalize();

                    // Convert normal to WORLD space
                    Point3D worldZero = view.localToScene(Point3D.ZERO);
                    Point3D worldNormalEnd = view.localToScene(normalLocal);
                    bestNormal = worldNormalEnd.subtract(worldZero).normalize();
                }
            }
        }

        if (bestHit == null)
            return null;

        // convert hit point to world space
        Point3D hitWorld = view.localToScene(bestHit);

        return new PickResult(
            view.getModelPiece(),                     // which piece was hit
            (LegoPieceMesh) view.getMesh(),           // mesh
            hitWorld,                                 // hit point in world space
            bestNormal                                // face normal in world space
        );
    }


    // Computes the pick ray originating from the camera through the screen pixel.
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

    // Data structure to hold picking results ==================================================

    protected static class PickResult {
        public final LegoPiece piece;
        public final LegoPieceMesh mesh;
        public final Point3D hitPoint;
        public final Point3D faceNormal;

        public PickResult(LegoPiece piece, LegoPieceMesh mesh,
                          Point3D hitPoint, Point3D faceNormal) {
            this.piece = piece;
            this.mesh = mesh;
            this.hitPoint = hitPoint; // point of intersection in world coordinates
            this.faceNormal = faceNormal;  // direction of placement (face normal of selected triangle)

        }

        private double originToHitDistance(Point3D rayOrigin) {
            return hitPoint.distance(rayOrigin);
        }
    }

    // Ray structure ==================================================================

    protected static class Ray {
        public final Point3D origin;
        public final Point3D direction;

        public Ray(Point3D origin, Point3D direction) {
            this.origin = origin;
            this.direction = direction.normalize();
        }
    }

    // Mode switching =================================================================

    public void setMode(Mode mode) {
        this.currentMode = mode;
    }

    public Mode getMode() {
        return currentMode;
    }

}



