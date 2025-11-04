package edu.epfl.mklego.lxfml;

import java.util.List;

public class PartsManager {
    
    public static record Part (int designId, int numberRows, int numberColumns) {}

    public static final List<Part> parts = List.of(
        new Part(3005, 1, 1),
        new Part(3004, 1, 2),
        new Part(3622, 1, 3),
        new Part(3010, 1, 4),
        new Part(3009, 1, 6),
        new Part(3008, 1, 8),
        new Part(6111, 1, 10),
        new Part(6112, 1, 12),
        new Part(2465, 1, 16),
        new Part(3003, 2, 2),
        new Part(3002, 2, 3),
        new Part(3001, 2, 4),
        new Part(2456, 2, 6),
        new Part(3007, 2, 8),
        new Part(3006, 2, 10),
        new Part(2356, 4, 6),
        new Part(6212, 4, 10),
        new Part(4202, 4, 12),
        new Part(30400, 4, 18),
        new Part(4201, 8, 8),
        new Part(4204, 8, 16),
        new Part(30072, 12, 24)
    );

    public static Part getPartFromDesignId (int designId) {
        return parts.stream()
            .filter(part -> part.designId == designId)
            .findFirst()
            .orElse(null);
    }

}
