package edu.epfl.mklego.lgcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import edu.epfl.mklego.project.scene.entities.LegoPiece;
import edu.epfl.mklego.project.scene.entities.LegoPiece.StdLegoPieceKind;
import javafx.scene.paint.Color;

public class SupportGenerator {

    public static Color TRANSPARENT_COLOR = Color.color(0.5, 0.5, 0.5, 0.3);
    private static record Position (int x, int y, int z) {}
    private static record BrickType (String brickType, String color) {}
    private static int recScore (
            int[][][] hasPiece,
            int numberRows,
            int numberCols,
            Map<Position, Integer> cache,
            Map<Position, Position> path,
            Position pos
        ) {
        System.out.println("COMPUTE POSITION " + pos.x + " " + pos.y + " " + pos.z);
        if (pos.z < 0) {
            return 0;
        }
        if (pos.x < 0 || pos.y < 0 || pos.x + 1 >= numberRows || pos.y + 1 >= numberCols) {
            System.out.println(" OUT OF BOUNDS");
            return 1000;
        }

        if (cache.containsKey(pos)) {
            System.out.println(" CACHE HIT");
            return cache.get(pos);
        }

        int paa = hasPiece[pos.x][pos.y][pos.z];
        int pba = hasPiece[pos.x + 1][pos.y][pos.z];
        int pab = hasPiece[pos.x][pos.y + 1][pos.z];
        int pbb = hasPiece[pos.x + 1][pos.y + 1][pos.z];

        int local = (paa + pbb == 2 || pab + pba == 2) ? 0 : 1;
        
        paa = pos.z == 0 ? 0 : hasPiece[pos.x][pos.y][pos.z - 1];
        pba = pos.z == 0 ? 0 : hasPiece[pos.x + 1][pos.y][pos.z - 1];
        pab = pos.z == 0 ? 0 : hasPiece[pos.x][pos.y + 1][pos.z - 1];
        pbb = pos.z == 0 ? 0 : hasPiece[pos.x + 1][pos.y + 1][pos.z - 1];

        List<Position> targets = new ArrayList<>();
        if (paa + pba == 0) targets.add(new Position(pos.x, pos.y - 1, pos.z - 1));
        if (pab + pbb == 0) targets.add(new Position(pos.x, pos.y + 1, pos.z - 1));
        if (paa + pab == 0) targets.add(new Position(pos.x - 1, pos.y, pos.z - 1));
        if (pba + pbb == 0) targets.add(new Position(pos.x + 1, pos.y, pos.z - 1));
        
        if (paa + pbb + pab + pba == 0) {
            targets.clear();
            targets.add(new Position(pos.x, pos.y, pos.z - 1));
        }

        List<Integer> scores = targets
            .stream()
            .map(tar -> local + recScore(hasPiece, numberRows, numberCols, cache, path, tar))
            .toList();
        
        int best = 1000;
        int bi   = -1;
        for (int i = 0; i < scores.size(); i ++) {
            if (scores.get(i) < best) {
                best = scores.get(i);
                bi = i;
            }
        }

        cache.put(pos, best);
        if (bi == -1) return 1000;

        path.put(pos, targets.get(bi));
        return best;
    }
    
    public static LegoAssembly createSupport (LegoAssembly assembly) {
        int numberRows = assembly.getPlateNumberRows();
        int numberCols = assembly.getPlateNumberColumns();

        List<LegoPiece> finalPieces = new ArrayList<>();
        List<LegoPiece> basePieces = assembly.getPieces()
            .stream()
            .sorted((p1, p2) -> Integer.compare(p1.getMainStubHeight(), p2.getMainStubHeight()))
            .toList();
        List<LegoPiece> supportPieces = new ArrayList<>();

        if (basePieces.size() == 0) {
            return new LegoAssembly(numberRows, numberCols, finalPieces);
        }

        int maxHeight = basePieces.stream().map(LegoPiece::getMainStubHeight).max(Integer::compare).get();

        int[][][] hasPiece = new int[numberRows][numberCols][maxHeight + 1];

        for (LegoPiece piece : basePieces) {
            StdLegoPieceKind kind = (StdLegoPieceKind) piece.getKind();
            for (int drow = 0; drow < kind.getNumberRows(); drow ++) {
                for (int dcol = 0; dcol < kind.getNumberColumns(); dcol ++) {
                    hasPiece[
                        piece.getMainStubRow() + drow
                    ][
                        piece.getMainStubCol() + dcol
                    ][ piece.getMainStubHeight() ] = 1;
                }
            }
        }

        for (LegoPiece piece : basePieces) {
            finalPieces.add(piece);
            StdLegoPieceKind kind = (StdLegoPieceKind) piece.getKind();
            Map<Position, Integer> cache = new HashMap<>();
            Map<Position, Position> path = new HashMap<>();

            List<Position> targets = new ArrayList<>();
            for (int drow = 0; drow + 1 < kind.getNumberRows(); drow ++) {
                for (int dcol = 0; dcol + 1 < kind.getNumberColumns(); dcol ++) {
                    targets.add(new Position(piece.getMainStubRow() + drow, piece.getMainStubCol() + dcol, piece.getMainStubHeight()));
                }
            }

            int best = 1000;
            int bi = -1;
            for (int i = 0; i < targets.size(); i ++) {
                int sc = recScore(hasPiece, numberRows, numberCols, cache, path, targets.get(i));
                if (sc < best) {
                    best = sc;
                    bi = i;
                }
            }

                System.out.println("LEGO PIECE: " + piece.getMainStubRow() + " " + piece.getMainStubCol());
                System.out.println(" BEST : " + bi + " " + best);
            if (bi != -1 && best != 0) {
                Position pos = targets.get(bi);
                while (pos != null && pos.z >= 0) {
                    System.out.println(" POSITION : " + pos.x + " " + pos.y + " " + pos.z);
                    if (hasPiece[pos.x][pos.y][pos.z]
                      + hasPiece[pos.x + 1][pos.y][pos.z]
                      + hasPiece[pos.x][pos.y + 1][pos.z]
                      + hasPiece[pos.x + 1][pos.y + 1][pos.z] == 0
                    ) {
                        supportPieces.add(
                            new LegoPiece(
                                pos.x,
                                pos.y,
                                pos.z,
                                TRANSPARENT_COLOR,
                                new StdLegoPieceKind(2, 2))
                        );
                        hasPiece[pos.x][pos.y][pos.z] = 1;
                        hasPiece[pos.x][pos.y + 1][pos.z] = 1;
                        hasPiece[pos.x + 1][pos.y][pos.z] = 1;
                        hasPiece[pos.x + 1][pos.y + 1][pos.z] = 1;
                    }

                    pos = path.getOrDefault(pos, null);
                }
            }
        }

        while (supportPieces.size() != 0) {
            LegoPiece p1 = supportPieces.getLast();
            supportPieces.removeLast();

            LegoPiece po = null;
            for (LegoPiece p2 : supportPieces) {
                if (p1.getMainStubHeight() != p2.getMainStubHeight()) continue ;
                int dr = Math.abs(p1.getMainStubRow() - p2.getMainStubRow());
                int dc = Math.abs(p1.getMainStubCol() - p2.getMainStubCol());
                if (dr + dc > 2) continue ;

                int minr = Math.min(p1.getMainStubRow(), p2.getMainStubRow());
                int maxr = Math.max(p1.getMainStubRow(), p2.getMainStubRow());
                int minc = Math.min(p1.getMainStubCol(), p2.getMainStubCol());
                int maxc = Math.max(p1.getMainStubCol(), p2.getMainStubCol());
                finalPieces.add(
                    new LegoPiece(
                        minr, minc, p1.getMainStubHeight(),
                        TRANSPARENT_COLOR,
                        new StdLegoPieceKind(
                            minr == maxr ? 2 : 4,
                            minc == maxc ? 2 : 4)
                    )
                );

                po = p2;
                break ;
            }

            if (po != null) {
                supportPieces.remove(po);
            } else finalPieces.add(p1);
        }

        System.out.println("====== BILL OF MATERIALS ======");
        Map<BrickType, Integer> counters = new HashMap<>();
        for (LegoPiece piece : finalPieces) {
            StdLegoPieceKind kind = (StdLegoPieceKind) piece.getKind();

            int min = Math.min(kind.getNumberColumns(), kind.getNumberRows());
            int max = Math.max(kind.getNumberColumns(), kind.getNumberRows());

            BrickType type = new BrickType(
                max + "x" + min, piece.getColor().toString());
            counters.put(type, counters.getOrDefault(type, 0) + 1);
        }

        for (var type_and_count : counters.entrySet()) {
            System.out.println(type_and_count.getKey() + " -> " + type_and_count.getValue());
        }

        return new LegoAssembly(numberRows, numberCols, finalPieces);
    }

}
