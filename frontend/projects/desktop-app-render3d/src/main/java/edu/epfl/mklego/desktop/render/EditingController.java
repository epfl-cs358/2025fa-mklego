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
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

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
        System.out.println("Mouse pressed at: " + e.getX() + ", " + e.getY());
        if (!isEnabled()){
            System.out.println("NOT ENABLED");
            return;
        }

        PickResult pick = pickLegoPiece(scene, e.getX(), e.getY());
        if (pick == null) {
            clearSelection();
            System.out.println("NO PICK");
            return;
        }
        System.out.println("PIECE PICKED");
        switch (currentMode) {
            case SELECT -> setSelection(pick);
            case DELETE -> {System.out.println("ENTERED DELETE"); deletePiece(pick);}
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
        protected PickResult pickLegoPiece(Scene3D scene, double localX, double localY) {
        Ray pickRay = computePickRay(scene, localX, localY);
        if (pickRay == null){
            System.out.println("NULL RAY");
            return null;
        }
        System.out.println("RAY ORIGIN: " + pickRay.origin + " DIRECTION: " + pickRay.direction);

        List<LegoMeshView> pieceViews = scene.getAllPieceViews();

        PickResult nearest = null;
        double nearestDist = Double.POSITIVE_INFINITY;

        for (LegoMeshView view : pieceViews) {

            // mesh in local coordinates
            LegoPieceMesh mesh = (LegoPieceMesh) view.getMesh();
            if (mesh == null)
                continue;

            // transform ray into the mesh’s local coordinate system
            /*Point3D localOrigin = view.sceneToLocal(pickRay.origin);
            Point3D localEnd    = view.sceneToLocal(pickRay.origin.add(pickRay.direction));
            Point3D localDir    = localEnd.subtract(localOrigin).normalize();*/

            //var sceneToLocal = view.getLocalToSceneTransform().createInverse();
            Transform sceneToLocal;
            try {
                sceneToLocal = view.getLocalToSceneTransform().createInverse();
            } catch (NonInvertibleTransformException e) {
                // If we can’t invert, skip this piece (it shouldn’t happen in normal scenes)
                continue;
            }
            Point3D localOrigin = sceneToLocal.transform(pickRay.origin);
            Point3D localDir    = sceneToLocal.deltaTransform(pickRay.direction).normalize();

            PickResult hit = intersectWithPiece(localOrigin, localDir, mesh, view);

            if (hit != null) {
                System.out.println(" HIT PIECE: " + hit.piece);
                double dist = hit.originToHitDistance(pickRay.origin);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = hit;
                }
            }
            else System.out.println("NO HIT FOR PIECE: " + view.getModelPiece());
        }
        System.out.println("NEAREST PICK: " + (nearest != null ? nearest.piece : "null"));
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
        int stride = mesh.getFaceElementSize();       // element size PER FACE (here 6 ints)
        System.out.println("stride: " + stride);
        
        Point3D bestHit = null;
        Point3D bestNormal = null;
        double nearestDist = Double.POSITIVE_INFINITY;

        for (int i = 0; i < faces.length; i += stride) {
            
            int p0Index = faces[i]     * 3;
            int p1Index = faces[i+2]   * 3;
            int p2Index = faces[i+4]   * 3;

            Point3D v0 = new Point3D(pts[p0Index], pts[p0Index+1], pts[p0Index+2]);
            Point3D v1 = new Point3D(pts[p1Index], pts[p1Index+1], pts[p1Index+2]);
            Point3D v2 = new Point3D(pts[p2Index], pts[p2Index+1], pts[p2Index+2]);

            // ---- Möller–Trumbore ----
            Point3D edge1 = v1.subtract(v0);
            Point3D edge2 = v2.subtract(v0);

            // pvec
            Point3D p = rayDirLocal.crossProduct(edge2);
            double det = edge1.dotProduct(p);
            if (Math.abs(det) < 1e-7) continue;   // parallel

            double invDet = 1.0 / det;

            // u
            Point3D t = rayOriginLocal.subtract(v0);
            double u = t.dotProduct(p) * invDet;
            if (u < 0.0 || u > 1.0) continue;

            // v
            Point3D q = t.crossProduct(edge1);
            double v = rayDirLocal.dotProduct(q) * invDet;
            if (v < 0.0 || (u + v) > 1.0) continue;

            // t (distance along ray)
            double tHit = edge2.dotProduct(q) * invDet;
            if (tHit <= 1e-7) continue;    // behind the origin or too close

            Point3D hitLocal = rayOriginLocal.add(rayDirLocal.multiply(tHit));

                // Intersection at rayOriginLocal + t*rayDirLocal
                //Point3D hitLocal = rayOriginLocal.add(rayDirLocal.multiply(t));

                double dist = hitLocal.distance(rayOriginLocal);
                System.out.println("coucou1");
                if (dist < nearestDist) {
                    System.out.println("coucou2");
                    nearestDist = dist;
                    bestHit = hitLocal;
                    System.out.println("hitLocal: " + hitLocal);

                    // Compute normal in LOCAL space
                    Point3D normalLocal = edge1.crossProduct(edge2).normalize();

                    // Convert normal to WORLD space
                    Point3D worldZero = view.localToScene(Point3D.ZERO);
                    Point3D worldNormalEnd = view.localToScene(normalLocal);
                    bestNormal = worldNormalEnd.subtract(worldZero).normalize();
                }
            
        }

        if (bestHit == null){
            System.out.println("NO HIT FOUND IN MESH");
            return null;
        }

        // convert hit point to world space
        Point3D hitWorld = view.localToScene(bestHit);

        System.out.println(" HIT POINT LOCAL: " + bestHit + " WORLD: " + hitWorld + " NORMAL: " + bestNormal);
        return new PickResult(
            view.getModelPiece(),                     // which piece was hit
            (LegoPieceMesh) view.getMesh(),           // mesh
            hitWorld,                                 // hit point in world space
            bestNormal                                // face normal in world space
        );
    }


    // Computes the pick ray originating from the camera through the screen pixel.
    /*protected Ray computePickRay(Scene3D scene, double screenX, double screenY) {
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
        protected Ray computePickRay(Scene3D scene, double localX, double localY) {
        Camera cam = scene.getCamera();
        if (!(cam instanceof PerspectiveCamera camera)) {
            return null;
        }

        double w = scene.getWidth();
        double h = scene.getHeight();
        if (w <= 0 || h <= 0) return null;

        // 1) Normalized device coords (-1..1, -1..1)
        double ndcX = (2.0 * localX / w) - 1.0;   // left = -1, right = +1
        double ndcY = 1.0 - (2.0 * localY / h);   // top = +1, bottom = -1

        double fovRad = Math.toRadians(camera.getFieldOfView());
        double tan = Math.tan(fovRad / 2.0);
        double aspect = w / h;

        // 2) Ray direction in CAMERA-LOCAL space
        // JavaFX camera looks along -Z
        double dx = ndcX * aspect * tan;
        double dy = ndcY * tan;
        double dz = -1.0;

        Point3D dirLocal = new Point3D(dx, dy, dz).normalize();

        // 3) Convert origin and direction to WORLD space
        Point3D originWorld = camera.localToScene(Point3D.ZERO);
        Point3D dirWorldEnd = camera.localToScene(dirLocal);

        Point3D dirWorld = dirWorldEnd.subtract(originWorld).normalize();

        return new Ray(originWorld, dirWorld);
    }*/


    protected Ray computePickRay(Scene3D scene, double localX, double localY) {
        Camera cam = scene.getCamera();
        if (!(cam instanceof PerspectiveCamera camera)) {
            return null;
        }

        double w = scene.getWidth();
        double h = scene.getHeight();
        if (w <= 0 || h <= 0) return null;

        // NDC in [-1,1]
        double ndcX = (2.0 * localX / w) - 1.0;       // left=-1, right=+1
        double ndcY = 1.0 - (2.0 * localY / h);       // top=+1, bottom=-1  (JavaFX Y-down is fine here)

        double fovRad = Math.toRadians(camera.getFieldOfView());   // vertical FOV
        double tanY = Math.tan(fovRad / 2.0);
        double aspect = w / h;
        double tanX = tanY * aspect;

        // Direction in CAMERA-LOCAL space (camera looks along -Z in JavaFX)
        //Point3D dirLocal = new Point3D(ndcX * tanX, ndcY * tanY, -1.0).normalize();
        Point3D dirLocal = new Point3D(ndcX * tanX, ndcY * tanY, 1.0).normalize();


        // Use proper transforms: transform origin as a point, direction as a delta (no translation)
        var camToScene = camera.getLocalToSceneTransform(); // in the SubScene's coord space
        Point3D originWorld = camToScene.transform(Point3D.ZERO);
        Point3D dirWorld    = camToScene.deltaTransform(dirLocal).normalize();

        return new Ray(originWorld, dirWorld);
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



