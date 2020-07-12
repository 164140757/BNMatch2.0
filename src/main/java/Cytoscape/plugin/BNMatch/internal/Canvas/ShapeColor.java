package Cytoscape.plugin.BNMatch.internal.Canvas;

import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.jgrapht.alg.util.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class ShapeColor {
    public static Collection<Color> colors = new HashSet<>(Arrays.asList(Color.LIGHT_GRAY,Color.magenta,Color.BLUE,
            Color.cyan,Color.GRAY,Color.green,Color.ORANGE,Color.pink,Color.red,Color.YELLOW));
    public static Collection<NodeShape> shapes = new HashSet<>(Arrays.asList(NodeShapeVisualProperty.TRIANGLE,
            NodeShapeVisualProperty.ELLIPSE, NodeShapeVisualProperty.HEXAGON, NodeShapeVisualProperty.OCTAGON,
            NodeShapeVisualProperty.PARALLELOGRAM, NodeShapeVisualProperty.RECTANGLE, NodeShapeVisualProperty.TRIANGLE));
    private static ArrayList<Pair<Color, NodeShape>> colorShapeList;
    private static ArrayList<Color> colorList;

    public static Pair<Color, NodeShape> randomShapeAndColor(){
        int sum = colors.size()* shapes.size();
        int rand = (int)(Math.random()*sum);
        if(colorShapeList == null){
            colorShapeList = new ArrayList<>();
            colors.forEach(c->shapes.forEach(s-> colorShapeList.add(new Pair<>(c, s))));
        }
        return colorShapeList.get(rand);
    }
    public static Color randomColor(){
        int sum = colors.size();
        int rand = (int)(Math.random()*sum);
        if(colorList == null){
            colorList = new ArrayList<>();
            colorList.addAll(colors);
        }
        return colorList.get(rand);
    }
}
