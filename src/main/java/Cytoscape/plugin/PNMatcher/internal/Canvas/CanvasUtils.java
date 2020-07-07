package Cytoscape.plugin.PNMatcher.internal.Canvas;

import org.cytoscape.view.presentation.property.values.NodeShape;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cytoscape.view.presentation.property.NodeShapeVisualProperty.*;

public class CanvasUtils {

    public static final String[] colorStr = { "red", "green", "blue", "cyan",
            "magenta", "green2", "black" };

    public static final String[] shapeStr = { "diamond", "ellipse", "hexagon",
            "octagon", "parallelogram", "rect", "triangle" };
    static List<ColourShape> shapeColor = null;
    /** Map for converting Strings to colors */
    public static final Map<String, String> COLORSTRINGS = Collections
            .unmodifiableMap(new HashMap<String, String>() {
                {
                    put("red", CanvasUtils.getColor(255, 0, 0));
                    put("green", CanvasUtils.getColor(0, 255, 0));
                    put("blue", CanvasUtils.getColor(0, 0, 255));
                    put("cyan", CanvasUtils.getColor(255, 0, 255));
                    put("magenta", CanvasUtils.getColor(0, 255, 255));
                    put("green2", CanvasUtils.getColor(0, 102, 51));
                    put("black", CanvasUtils.getColor(0, 51, 51));
                }
            });

    /** Map for converting Strings to node shapes. */
    public static final Map<String, NodeShape> SHAPESTRINGS = Collections
            .unmodifiableMap(new HashMap<String, NodeShape>() {
                {
                    put("diamond", DIAMOND);
                    put("ellipse", ELLIPSE);
                    put("hexagon", HEXAGON);
                    put("octagon", OCTAGON);
                    put("parallelogram", PARALLELOGRAM);
                    put("rect", RECTANGLE);
                    put("triangle", TRIANGLE);
                }
            });

    /**
     * Creates a color string as used by Cytoscape from the specified red, green
     * and blue parameters.
     *
     * @param r
     *            red component
     * @param g
     *            green component
     * @param b
     *            blue component
     * @return a string containing the color representation used in Cytoscape.
     */
    public static String getColor(final int r, final int g, final int b) {
        if (r < 0 || r > 255) {
            System.err.println("Invalid value for red, please specify "
                    + "an integer between 0 and 255.");
        }
        if (g < 0 || g > 255) {
            System.err.println("Invalid value for green, please specify "
                    + "an integer between 0 and 255.");
        }
        if (b < 0 || b > 255) {
            System.err.println("Invalid value for blue, please specify "
                    + "an integer between 0 and 255.");
        }
        return r + "," + g + "," + b;
    }
}

class ColourShape {
    public String color, shape;
    public Boolean IsSame(ColourShape s) {
        return color.equals(s.color) && shape.equals(s.shape);
    }
}
