package edu.epfl.mklego.lxfml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.epfl.mklego.desktop.render.mesh.LegoPieceMesh;
import edu.epfl.mklego.lxfml.PartsManager.Part;
import edu.epfl.mklego.project.scene.ProjectScene;
import edu.epfl.mklego.project.scene.Transform;
import edu.epfl.mklego.project.scene.Transform.Observable3f;
import edu.epfl.mklego.project.scene.entities.LegoAssembly;

public class LXFMLReader {
    private static float score (List<Float> A, List<Float> B) {
      float res = 0;
      for (int i = 0; i < A.size(); i ++) 
        res += Math.abs(A.get(i) - B.get(i)); 
      return res;
    }
    public static LegoAssembly createAssembly (InputStream stream) {
        try {
            LegoAssembly assembly = new LegoAssembly(22, 22, List.of());

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            //double rnd = Math.random();
            //InputStream stream = rnd < 0.5 ? new ByteArrayInputStream(SAMPLE.getBytes("UTF-8")) : LXFMLReader.class.getResourceAsStream("lxfmtext2.csv");
            Document document = dBuilder.parse(stream);

            Node bricks = getByName(document.getDocumentElement(), "Bricks");
            
            List<Node> brickList = filterByName(bricks, "Brick");
            int idx = 0;
            for (Node brick : brickList) {
                idx ++;

                Node part = getByName(brick, "Part");
                Node bone = getByName(part,  "Bone");

                int designId = Integer.parseInt(
                    part.getAttributes().getNamedItem("designID").getNodeValue());
                int colorId = Integer.parseInt(
                    part.getAttributes().getNamedItem("materials").getNodeValue().split(",")[0]);
                
                String[] transformation = bone.getAttributes().getNamedItem("transformation").getNodeValue().split(",");
                float mxx = Float.parseFloat(transformation[0]);
                float mxz = Float.parseFloat(transformation[2]);
                float mzx = Float.parseFloat(transformation[6]);
                float mzz = Float.parseFloat(transformation[8]);

                List<Float> fB = List.of(mxx, mxz, mzx, mzz);
                List<Float> f0 = List.of(+ 1.f, + 0.f, + 0.f, + 1.f);
                List<Float> f1 = List.of(+ 0.f, - 1.f, + 1.f, + 0.f);
                List<Float> f2 = List.of(- 1.f, + 0.f, + 0.f, - 1.f);
                List<Float> f3 = List.of(+ 0.f, + 1.f, - 1.f, + 0.f);

                int mode = -1;
                if (score(fB, f0) <= 1e-4) mode = 0;
                if (score(fB, f1) <= 1e-4) mode = 1;
                if (score(fB, f2) <= 1e-4) mode = 2;
                if (score(fB, f3) <= 1e-4) mode = 3;
                System.out.println(mxx + " " + mxz + " " + mzx + " " + mzz);
                System.out.println(mode);

                float tx = 10.f * Float.parseFloat(transformation[9]);
                float ty = 10.f * Float.parseFloat(transformation[10]);
                float tz = 10.f * Float.parseFloat(transformation[11]);

                Part legoPart = PartsManager.getPartFromDesignId(designId);

                int numberColumns = ((mode & 1) == 0) ? legoPart.numberColumns() : legoPart.numberRows();
                int numberRows = ((mode & 1) == 0) ? legoPart.numberRows() : legoPart.numberColumns();
                
                float dNumberColumns = ((float) numberColumns) / 2.0f - 0.5f;
                float dNumberRows    = ((float) numberRows) / 2.0f - 0.5f;

                float fx = 0, fy = 0;
                if (mode == 0) {
                  fx = 1;
                  fy = -1;
                } else if (mode == 1) {
                  fx = - 1;
                  fy = - 1; 
                } else if (mode == 2) {
                  fx = -1;
                  fy = 1;
                } else if (mode == 3) {
                  fx = 1;
                  fy = 1;
                }

                System.out.println(dNumberRows + " " + dNumberColumns);

                Transform transform = new Transform(
                    new Observable3f(1, 1, 1), 
                    new Observable3f(
                        - (tx + fx * dNumberColumns * LegoPieceMesh.LEGO_WIDTH),
                        (tz + fy * dNumberRows * LegoPieceMesh.LEGO_WIDTH), ty), 
                    new Observable3f(0, 0, 0));

                // TODO
                //LegoPieceEntity entity = new LegoPieceEntity(
                //    transform, 
                //    "Brick #" + idx, 
                //    ColorManager.getInstance().fromLegoId(colorId).color(),
                //    numberColumns,
                //    numberRows
                //);
            }

            return assembly;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static List<Node> childrens (Node element) {
        List<Node> res = new ArrayList<>();
        NodeList list = element.getChildNodes();
        for (int idx = 0; idx < list.getLength(); idx ++) {
            Node child = list.item(idx);
            res.add(child);
        }
        return res;
    }
    private static List<Node> filterByName (Node element, String name) {
        return childrens(element)
            .stream()
            .filter(el -> el.getNodeName().equals(name))
            .toList();
    }
    private static Node getByName (Node element, String name) {
        return childrens(element)
            .stream()
            .filter(el -> el.getNodeName().equals(name))
            .findFirst()
            .orElse(null);
    }

}
