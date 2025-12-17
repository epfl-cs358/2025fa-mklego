package edu.epfl.mklego.lgcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.epfl.mklego.lgcode.config.AddBrick;
import edu.epfl.mklego.lgcode.config.AddColor;
import edu.epfl.mklego.lgcode.config.PlateSize;
import edu.epfl.mklego.lgcode.format.Serializable;
import edu.epfl.mklego.lgcode.format.SetSection;
import edu.epfl.mklego.lgcode.print.Drop;
import edu.epfl.mklego.lgcode.print.Grab;
import edu.epfl.mklego.lgcode.print.Move;
import edu.epfl.mklego.lgcode.print.Rotate;
import edu.epfl.mklego.project.Project;
import edu.epfl.mklego.project.scene.entities.LegoAssembly;
import edu.epfl.mklego.project.scene.entities.LegoPiece;
import edu.epfl.mklego.project.scene.entities.LegoPiece.DuploLegoPieceKind;
import edu.epfl.mklego.project.scene.entities.LegoPiece.StdLegoPieceKind;
import javafx.scene.paint.Color;

public class ProjectConverter {
    private static record PartialBrickType (int nbcols, int nbrows, String colorHex) {}

    public static LGCode createCode (Project project) {
        LegoAssembly assembly = project.getScene()
            .getLegoAssembly();
        
        List<Color> colors = assembly.getPieces()
            .stream()
            .map( piece -> piece.getColor() )
            .distinct()
            .toList();
        
        Map<String, Integer> hexStringToColorId = new HashMap<>();

        List<Serializable> lgcode = new ArrayList<>();
        lgcode.add(SetSection.CONFIG_SECTION);
        // x ~ column, y ~ row
        lgcode.add(new PlateSize(assembly.getPlateNumberColumns(), assembly.getPlateNumberRows()));

        for (int colorId = 0; colorId < colors.size(); colorId ++) {
            hexStringToColorId.put(colors.get(colorId).toString(), colorId);

            lgcode.add(
                new AddColor(
                    colorId,
                    (int) Math.round(255.0 * colors.get(colorId).getRed()),
                    (int) Math.round(255.0 * colors.get(colorId).getGreen()),
                    (int) Math.round(255.0 * colors.get(colorId).getBlue()),
                    (int) Math.round(255.0 * colors.get(colorId).getOpacity()),
                    "",
                    ""
                )
            );
        }

        List<LegoPiece> pieces = new ArrayList<>( assembly.getPieces() );
        pieces.sort((p1, p2) -> {
            if (p1.getMainStubHeight() != p2.getMainStubHeight())
                return Integer.compare(
                    p1.getMainStubHeight(),
                    p2.getMainStubHeight()
                );
            if (p1.getMainStubRow() != p2.getMainStubRow())
                return Integer.compare(
                    p1.getMainStubRow(),
                    p2.getMainStubRow()
                );
            return Integer.compare(
                p1.getMainStubCol(),
                p2.getMainStubCol()
            );
        });

        int maxHeight = pieces.stream()
                .map(piece -> piece.getMainStubHeight() + 2)
                .max(Integer::compare)
                .orElse(0);

        float[][][] supportedScore = new float
            [ assembly.getPlateNumberRows() ][ assembly.getPlateNumberColumns() ][ maxHeight ];
        
        for (int row = 0; row < supportedScore.length; row ++) {
            for (int col = 0; col < supportedScore[row].length; col ++) {
                for (int height = 0; height < supportedScore[row][col].length; height ++) {
                    supportedScore[row][col][height] = height == 0 ? 1.f : 0.f;
                }
            }
        }

        List<List<LegoPiece>> stages = new ArrayList<>();
        for (int height = 0; height < maxHeight; height ++)
            stages.add(new ArrayList<>());
        
        for (LegoPiece piece : pieces) {
            System.out.println(piece.getMainStubHeight() + " " + piece.getMainStubRow() + " " + piece.getMainStubCol());
            stages.get(piece.getMainStubHeight() + 1).add(piece);
        }

        Map<PartialBrickType, Integer> brickIds = new HashMap<>();

        List<Serializable> placements = new ArrayList<>();
        for (int height = 1; height < maxHeight; height ++) {
            for (LegoPiece piece : stages.get(height)) {
                if (piece.getKind() instanceof DuploLegoPieceKind) continue;
                StdLegoPieceKind pieceKind = (StdLegoPieceKind) piece.getKind();

                PartialBrickType type = new PartialBrickType(
                    Math.max(pieceKind.getNumberRows(), pieceKind.getNumberColumns()), 
                    Math.min(pieceKind.getNumberRows(), pieceKind.getNumberColumns()), 
                    piece.getColor().toString());
                
                if (!brickIds.containsKey(type)) {
                    lgcode.add(
                        new AddBrick(
                            brickIds.size(),
                            "std_" + type.nbcols + "_" + type.nbrows,
                            hexStringToColorId.get(type.colorHex),
                            brickIds.size() + 1
                        ));
                    brickIds.put(type, brickIds.size());
                }

                int   bestRow = -1;
                int   bestCol = -1;
                int   placRow = -1;
                int   placCol = -1;
                float bestScr = -1.f;

                int nbRowsAttach = pieceKind.getNumberRows();
                int nbColsAttach = pieceKind.getNumberColumns();
                for (int iRow = 0; iRow + 1 < nbRowsAttach; iRow ++) {
                    for (int iCol = 0; iCol + 1 < nbColsAttach; iCol ++) {
                        int nRow = piece.getMainStubRow() + iRow;
                        int nCol = piece.getMainStubCol() + iCol;

                        float mean = 0.5f + 0.125f * (
                            supportedScore[nRow][nCol][height - 1]
                          + supportedScore[nRow + 1][nCol][height - 1]
                          + supportedScore[nRow][nCol + 1][height - 1]
                          + supportedScore[nRow + 1][nCol + 1][height - 1]
                        );

                        supportedScore[nRow][nCol][height] = Math.max(
                            supportedScore[nRow][nCol][height],
                            mean
                        );
                        supportedScore[nRow + 1][nCol][height] = Math.max(
                            supportedScore[nRow + 1][nCol][height],
                            mean
                        );
                        supportedScore[nRow][nCol + 1][height] = Math.max(
                            supportedScore[nRow][nCol + 1][height],
                            mean
                        );
                        supportedScore[nRow + 1][nCol + 1][height] = Math.max(
                            supportedScore[nRow + 1][nCol + 1][height],
                            mean
                        );

                        if (mean > bestScr) {
                            bestScr = mean;
                            bestRow = iRow;
                            bestCol = iCol;
                            placRow = nRow;
                            placCol = nCol;
                        }
                    }
                }

                boolean shouldRotate = pieceKind.getNumberColumns() < pieceKind.getNumberRows();
                
                int attachmentId;
                if (!shouldRotate)
                    attachmentId = bestCol + bestRow * pieceKind.getNumberColumns();
                else attachmentId = bestRow + bestCol * pieceKind.getNumberRows();
                
                placements.add(new Rotate(1));
                placements.add(
                    new Grab(brickIds.get(type), attachmentId)
                );

                if (shouldRotate)
                    placements.add(new Rotate(0));
                
                placements.add(new Move(placCol, placRow, piece.getMainStubHeight()));
                placements.add(new Drop());
            }
        }

        lgcode.add(SetSection.PRINT_SECTION);
        for (Serializable place : placements)
            lgcode.add(place);

        return new LGCode(lgcode);
    }
}