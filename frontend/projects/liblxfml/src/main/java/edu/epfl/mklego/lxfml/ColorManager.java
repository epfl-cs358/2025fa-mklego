package edu.epfl.mklego.lxfml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import javafx.scene.paint.Color;

public class ColorManager {
    
    public static record LegoColor (int internalId, String name, Color color, int legoId) {}

    private static ColorManager manager = null;
    
    private final List<LegoColor> colors;

    private ColorManager () {
        InputStream stream = this.getClass().getResourceAsStream("colors.csv");
    
        ArrayList<LegoColor> colors = new ArrayList<>();
        try {
            CSVParser parser = CSVFormat.DEFAULT.builder()
                .setHeader("id", "name", "rgb", "legoid")
                .get().parse(new InputStreamReader(stream));
            
            for (CSVRecord record : parser) {
                colors.add(
                    new LegoColor(
                        Integer.parseInt( record.get("id") ),
                        record.get("name"),
                        Color.web("#" + record.get("rgb")), 
                        Integer.parseInt( record.get("legoid") ))
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.colors = List.copyOf(colors);
        for (LegoColor color : colors) System.out.println(color);
    }

    public List<LegoColor> getColors () {
        return colors;
    }
    public LegoColor fromLegoId (int id) {
        return colors
            .stream()
            .filter(x -> x.legoId == id)
            .findFirst()
            .orElse(null);
    }

    public static ColorManager getInstance () {
        if (manager == null)
            manager = new ColorManager();
        return manager;
    }

}
