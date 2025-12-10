package edu.epfl.mklego.desktop.render;

import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import edu.epfl.mklego.project.scene.entities.LegoPiece;
import edu.epfl.mklego.project.scene.entities.LegoPiece.StdLegoPieceKind;
import edu.epfl.mklego.desktop.render.mesh.LegoMeshView;
import edu.epfl.mklego.desktop.render.mesh.LegoPieceMesh;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

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
        DELETE,
        ADD
    }

    private Mode currentMode = Mode.SELECT;

    // --- ADD mode configuration ---------------------------------------------

    /**
     * Current kind used in ADD mode (any rectangular brick, e.g. 1x2, 2x2, 2x4...).
     * numberRows = studs along "row" axis, numberColumns = studs along "column" axis.
     */
    private StdLegoPieceKind currentAddKind = new StdLegoPieceKind(2, 4); // default 2x4

    // --- ADD mode preview (ghost piece) -------------------------------------

    /** Visual ghost piece that follows the mouse in ADD mode. */
    private LegoMeshView previewView = null;
    /** Whether the preview node is currently visible. */
    private boolean previewVisible = false;

    /** Last valid preview grid position (for placing on click). */
    private boolean hasPreviewPosition = false;
    private int previewRow;
    private int previewCol;
    private int previewHeight;

    public EditingController() {
        super(true);
    }

    // Public API to change the kind used in ADD mode =========================

    public void rotatePreview() {
        StdLegoPieceKind newKind = new StdLegoPieceKind(currentAddKind.getNumberColumns(), currentAddKind.getNumberRows());            
        setCurrentAddKind(newKind);
    }

    public void setCurrentAddKind(StdLegoPieceKind kind) {
        if (kind == null) return;
        this.currentAddKind = kind;

        // Rebuild preview mesh to match new dimensions
        if (scene != null && previewView != null) {
            Group root3D = (Group) scene.getRoot();
            root3D.getChildren().remove(previewView);
            previewView = null;
            previewVisible = false;
            hasPreviewPosition = false;
            createAddPreviewNode();
        }
    }

    public StdLegoPieceKind getCurrentAddKind() {
        return currentAddKind;
    }

    // SceneController hooks ===================================================

    @Override
    public void control(Scene3D scene) {
        this.scene = scene;

        // Attach mouse listeners
        scene.addEventHandler(MouseEvent.MOUSE_PRESSED,  this::handleMousePressed);

        // hover preview for ADD mode
        scene.addEventHandler(MouseEvent.MOUSE_MOVED,  this::handleMouseMoved);
        scene.addEventHandler(MouseEvent.MOUSE_EXITED, this::handleMouseExited);

        // Lazy-created, but we can also create now
        createAddPreviewNode();
    }

    @Override
    public void dispose(Scene3D scene) {
        // Remove listeners to avoid memory leaks
        scene.removeEventHandler(MouseEvent.MOUSE_PRESSED,  this::handleMousePressed);

        scene.removeEventHandler(MouseEvent.MOUSE_MOVED,  this::handleMouseMoved);
        scene.removeEventHandler(MouseEvent.MOUSE_EXITED, this::handleMouseExited);
    }

    // Mouse handlers ==========================================================

    private void handleMousePressed(MouseEvent e) {
        if (!isEnabled()){
            return;
        }

        PickResult pick = pickLegoPiece(scene, e.getX(), e.getY());
            if (pick == null) {
                clearSelection();
                return;
            }

        switch (currentMode) {
            case SELECT -> setSelection(pick);
            case DELETE -> deletePiece(pick);
            case ADD    -> {
                // placing where the ghost currently is.
                if (hasPreviewPosition) {
                    placePieceFromPreview();
                } 
                /* old behavior:
                else if (pick != null) {
                    addPieceOnFace(pick);
                }
                */
            }
        }
    }

    // Hover handlers for ADD mode ============================================

    private void handleMouseMoved(MouseEvent e) {
        if (!isEnabled())
            return;

        if (currentMode != Mode.ADD) {
            hideAddPreview();
            return;
        }

        updateAddPreview(e);
    }

    private void handleMouseExited(MouseEvent e) {
        hideAddPreview();
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


    // Delete logic ============================================================

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


    // Add piece logic =========================================================

    /** Old behavior: add on top of clicked face. */
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
            // Y dominant => front / back
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


    // Picking pipeline ========================================================

    //Returns the LEGO piece that the ray hits, if any (triangle-based).
    protected PickResult pickLegoPiece(Scene3D scene, double localX, double localY) {
        Ray pickRay = computePickRay(scene, localX, localY);
        if (pickRay == null){
            return null;
        }

        List<LegoMeshView> pieceViews = scene.getAllPieceViews();

        PickResult nearest = null;
        double nearestDist = Double.POSITIVE_INFINITY;

        for (LegoMeshView view : pieceViews) {

            // mesh in local coordinates
            LegoPieceMesh mesh = (LegoPieceMesh) view.getMesh();
            if (mesh == null)
                continue;

            // transform ray into the mesh’s local coordinate system
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
        int stride = mesh.getFaceElementSize();       // element size PER FACE (here 6 ints)

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

        if (bestHit == null){
            return null;
        }

        // convert hit point to world space
        Point3D hitWorld = view.localToScene(bestHit);

        return new PickResult(
            view.getModelPiece(),                     // which piece was hit
            (LegoPieceMesh) view.getMesh(),           // mesh
            hitWorld,                                 // hit point in world space
            bestNormal                                // face normal in world space
        );
    }


    protected Ray computePickRay(Scene3D scene, double localX, double localY) {
        Camera cam = scene.getCamera();
        if (!(cam instanceof PerspectiveCamera camera)) {
            return null;
        }

        double w = scene.getWidth();
        double h = scene.getHeight();
        if (w <= 0 || h <= 0) return null;

        // 1) Normalized device coords in [-1, 1]
        double ndcX = (2.0 * localX / w) - 1.0;
        double ndcY = -(1.0 - (2.0 * localY / h));

        // 2) Take camera FOV configuration into account
        double fovRad = Math.toRadians(camera.getFieldOfView());
        double aspect = w / h;
        double tanHalfFov = Math.tan(fovRad / 2.0);

        double sy = tanHalfFov;
        double sx = tanHalfFov * aspect;

        // 3) Direction in camera-local space (camera looks along +Z in JavaFX)
        Point3D dirLocal = new Point3D(ndcX * sx, ndcY * sy, 1.0).normalize();

        // 4) Transform to world / subscene coordinates
        Transform camToScene = camera.getLocalToSceneTransform();
        Point3D originWorld = camToScene.transform(Point3D.ZERO);
        Point3D dirWorld    = camToScene.deltaTransform(dirLocal).normalize();

        // DEBUG
        showDebugRay(originWorld, dirWorld);
        return new Ray(originWorld, dirWorld);
    }



    // Data structure to hold picking results =================================

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

    // Ray structure ===========================================================

    protected static class Ray {
        public final Point3D origin;
        public final Point3D direction;

        public Ray(Point3D origin, Point3D direction) {
            this.origin = origin;
            this.direction = direction.normalize();
        }
    }

    // === ADD preview implementation =========================================

    /** Create the ghost mesh once and attach it to the scene root. */
    private void createAddPreviewNode() {
        if (scene == null || previewView != null) return;

        Group root3D = (Group) scene.getRoot();

        int kindRows    = currentAddKind.getNumberRows();
        int kindColumns = currentAddKind.getNumberColumns();

        previewView = LegoMeshView.makePiece(
            kindColumns,  // numberColumns in mesh ctor
            kindRows,     // numberRows in mesh ctor
            Color.color(1.0, 1.0, 0.0, 0.4)
        );
        previewView.setMouseTransparent(true);
        previewView.setVisible(false);
        previewVisible = false;

        root3D.getChildren().add(previewView);
    }

    private void showAddPreview() {
        if (previewView != null && !previewVisible) {
            previewView.setVisible(true);
            previewVisible = true;
        }
    }

    private void hideAddPreview() {
        if (previewView != null && previewVisible) {
            previewView.setVisible(false);
            previewVisible = false;
        }
        hasPreviewPosition = false;
    }

    /**
     * Update preview position from mouse ray.
     */
    private void updateAddPreview(MouseEvent e) {
        if (scene == null)
            return;

        ProjectScene sceneData = scene.getProjectScene();
        if (sceneData == null || sceneData.getLegoAssembly() == null)
            return;

        LegoAssembly assembly = sceneData.getLegoAssembly();

        if (previewView == null) {
            createAddPreviewNode();
            if (previewView == null) return;
        }

        int kindRows    = currentAddKind.getNumberRows();
        int kindColumns = currentAddKind.getNumberColumns();

        // 1) Get ray through mouse
        Ray ray = computePickRay(scene, e.getX(), e.getY());
        if (ray == null) {
            hideAddPreview();
            return;
        }

        // 2) Intersect with plane Z = top of plate
        Point3D hitOnGrid = intersectRayWithPlaneZ(ray, -LegoPieceMesh.LEGO_PARAMETER);

        if (hitOnGrid == null) {
            hideAddPreview();
            return;
        }

        // 3) Convert world X/Y to stub row/col indices, using brick size
        int baseCol = worldXToRow(assembly, hitOnGrid.getX(), kindRows);
        int baseRow = worldYToCol(assembly, hitOnGrid.getY(), kindColumns);

        // 4) Snap / clamp so the footprint stays inside the plate
        int maxRow = assembly.getPlateNumberRows()    - kindRows;
        int maxCol = assembly.getPlateNumberColumns() - kindColumns;

        baseRow = Math.max(0, Math.min(baseRow, maxRow));
        baseCol = Math.max(0, Math.min(baseCol, maxCol));

        // 5) Compute highest height in that footprint
        int topHeight = getMaxHeightForFootprint(assembly, baseRow, baseCol, kindRows, kindColumns);

        // Can only place piece on top of highest piece
        int h = (topHeight < 0 ? 0 : topHeight + 1);

        // 6) Update preview cached coords and transform
        this.previewRow = baseRow;
        this.previewCol = baseCol;
        this.previewHeight = h;
        this.hasPreviewPosition = true;

        updatePreviewTransform(assembly, baseRow, baseCol, h, kindRows, kindColumns);
        showAddPreview();
    }

    /**
     * Intersects ray with world plane Z = planeZ.
     */
    private Point3D intersectRayWithPlaneZ(Ray ray, double planeZ) {
        double oz = ray.origin.getZ();
        double dz = ray.direction.getZ();

        double EPS = 1e-8;
        if (Math.abs(dz) < EPS) {
            return null; // parallel to plane
        }

        double t = (planeZ - oz) / dz;
        if (t < 0) {
            return null; // behind camera
        }

        return ray.origin.add(ray.direction.multiply(t));
    }

    /**
     * Convert world X coordinate to LEGO row index using the same logic
     * as SceneRenderer.render(LegoAssembly, LegoPiece, StdLegoPieceKind).
     */
    private int worldXToRow(LegoAssembly assembly, double worldX, int pieceRows) {
        // deltaX = (row - totalRows/2) + (kindRows/2)
        // worldX = deltaX * LEGO_WIDTH
        float totalRows = assembly.getPlateNumberRows();
        double deltaX = worldX / LegoPieceMesh.LEGO_WIDTH;
        return (int)Math.round(deltaX + totalRows / 2.0 - pieceRows / 2.0);
    }

    /**
     * Convert world Y coordinate to LEGO column index (same idea as X).
     */
    private int worldYToCol(LegoAssembly assembly, double worldY, int pieceCols) {
        float totalCols = assembly.getPlateNumberColumns();
        double deltaY = worldY / LegoPieceMesh.LEGO_WIDTH;
        return (int)Math.round(deltaY + totalCols / 2.0 - pieceCols / 2.0);
    }

    /**
     * Recompute the previewView translation using the same math as SceneRenderer.
     * For a brick of size (kindRows x kindColumns) at (row, col, height).
     */
    private void updatePreviewTransform(LegoAssembly assembly,
                                        int row, int col, int height,
                                        int kindRows, int kindColumns) {
        if (previewView == null) return;

        int plateRows    = assembly.getPlateNumberRows();
        int plateColumns = assembly.getPlateNumberColumns();

        float deltaXtoStub = row - (plateRows    / 2.0f);
        float deltaYtoStub = col - (plateColumns / 2.0f);

        float deltaXstubToCenter = kindRows    / 2.0f;
        float deltaYstubToCenter = kindColumns / 2.0f;

        float deltaX = deltaXtoStub + deltaXstubToCenter;
        float deltaY = deltaYtoStub + deltaYstubToCenter;
        float deltaZ = height;

        double tx = deltaX * LegoPieceMesh.LEGO_WIDTH;
        double ty = deltaY * LegoPieceMesh.LEGO_WIDTH;
        double tz = deltaZ * (LegoPieceMesh.STANDARD_HEIGHT * LegoPieceMesh.LEGO_PARAMETER);

        previewView.setTranslateX(tx);
        previewView.setTranslateY(ty);
        previewView.setTranslateZ(tz);
    }


    /**
     * Scan all pieces and return the maximum mainStubHeight among pieces
     * overlapping the footprint whose top-left stub is (baseRow, baseCol)
     * and size is (footprintRows x footprintCols).
     */
    private int getMaxHeightForFootprint(LegoAssembly assembly,
                                         int baseRow, int baseCol,
                                         int footprintRows, int footprintCols) {
        int maxHeight = -1;

        int areaRowEnd = baseRow + footprintRows - 1;
        int areaColEnd = baseCol + footprintCols - 1;

        for (LegoPiece p : assembly.getPieces()) {
            if (!(p.getKind() instanceof StdLegoPieceKind kind)) {
                continue;
            }

            int prow = p.getMainStubRow();
            int pcol = p.getMainStubCol();
            int pheight = p.getMainStubHeight();

            int prowEnd = prow + kind.getNumberRows()    - 1;
            int pcolEnd = pcol + kind.getNumberColumns() - 1;

            boolean overlaps =
                (prow <= areaRowEnd) && (prowEnd >= baseRow) &&
                (pcol <= areaColEnd) && (pcolEnd >= baseCol);

            if (overlaps && pheight > maxHeight) {
                maxHeight = pheight;
            }
        }

        return maxHeight;
    }

    /**
     * When user clicks in ADD mode, create a real piece of currentAddKind where the ghost is.
     */
    private void placePieceFromPreview() {
        if (scene == null || !hasPreviewPosition) return;

        ProjectScene sceneData = scene.getProjectScene();
        if (sceneData == null || sceneData.getLegoAssembly() == null) return;

        LegoAssembly assembly = sceneData.getLegoAssembly();

        // Use currentAddKind and a default color (can be wired to UI later)
        StdLegoPieceKind kind = currentAddKind;
        Color color = Color.LIGHTGRAY;

        LegoPiece newPiece = new LegoPiece(
            previewRow,
            previewCol,
            previewHeight,
            color,
            kind
        );

        assembly.getPieces().add(newPiece);

        Group root3D = (Group) scene.getRoot();
        if (!root3D.getChildren().isEmpty()) {
            Node newSceneNode = new SceneRenderer().render(sceneData);
            root3D.getChildren().set(0, newSceneNode);
        }
    }


    // Mode switching ==========================================================

    public void setMode(Mode mode) {
        clearSelection();
        this.currentMode = mode;
        if (mode != Mode.ADD) {
            hideAddPreview();
        }
    }

    public Mode getMode() {
        return currentMode;
    }







    // Debug ===================================================================
/** Debug: draw the given pick ray as a red cylinder into the scene. */
    /**
     * Create a cylinder going from start to end in 3D space.
     */
    private Cylinder createCylinderBetween(Point3D start, Point3D end) {
        Point3D diff = end.subtract(start);
        double height = diff.magnitude();
        if (height < 1e-6) {
            height = 1e-6; // avoid zero-length cylinder
        }

        // radius big enough to be clearly visible
        double radius = 0.2;
        Cylinder cyl = new Cylinder(radius, height);

        // position at the midpoint
        Point3D mid = start.midpoint(end);
        cyl.getTransforms().add(new Translate(mid.getX(), mid.getY(), mid.getZ()));

        // orient cylinder: local Y axis -> diff direction
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D dir = diff.normalize();

        double dot = yAxis.dotProduct(dir);
        dot = Math.max(-1.0, Math.min(1.0, dot)); // clamp for acos
        double angle = Math.toDegrees(Math.acos(dot));

        Point3D rotAxis = yAxis.crossProduct(dir);
        if (rotAxis.magnitude() < 1e-6 || Double.isNaN(angle)) {
            // If nearly parallel, choose a default axis
            rotAxis = new Point3D(1, 0, 0);
            if (dir.getY() < 0) {
                angle = 180.0;
            } else {
                angle = 0.0;
            }
        }

        cyl.getTransforms().add(new Rotate(angle, rotAxis));

        // bright red material to see it clearly
        PhongMaterial material = new PhongMaterial(Color.RED);
        cyl.setMaterial(material);
        cyl.setMouseTransparent(true);
        return cyl;
    }
    private Cylinder lastDebugRay = null;
    /**
     * Show a long debug ray starting at originWorld, going along dirWorld.
     */
    private void showDebugRay(Point3D originWorld, Point3D dirWorld) {
        if (scene == null) {
            return;
        }

        Group root3D = (Group) scene.getRoot();
        if (root3D == null) {
            return;
        }

        // Remove previous debug ray if any
        if (lastDebugRay != null) {
            root3D.getChildren().remove(lastDebugRay);
            lastDebugRay = null;
        }

        // Make a long ray so it's clearly visible in the scene
        double length = 1000.0;  // adjust if you want shorter/longer
        Point3D end = originWorld.add(dirWorld.normalize().multiply(length));

        Cylinder rayNode = createCylinderBetween(originWorld, end);
        lastDebugRay = rayNode;
        root3D.getChildren().add(rayNode);
    }


}
