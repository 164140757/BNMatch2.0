package Cytoscape.plugin.BNMatch.internal.util;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public final class AlignmentTaskData {
    //HGA
    // Edge correctness
    public static double EC;
    // Edge score
    public static double ES;
    // Point and Edge Score
    public static double PE;
    // Point score
    public static double PS;
    // Total score
    public static double score;
    // mapping result for index vs target networks
    public static HashMap<CyNode, CyNode> cyNodeMapping;
    public static HashMap<String, String> mapping;
    public static Map<String, String> inverseMapping;
    // result to display
    public static CyNetwork combinedNet;

    public static HashMap<CyNode, CyNode> indexOldToNew;
    public static HashMap<CyNode, CyNode> targetOldToNew;
    public static Vector<Pair<DefaultWeightedEdge, DefaultWeightedEdge>> mappingEdges;
    public static HashMap<DefaultWeightedEdge, CyEdge> edgeCyEdgeMap1;
    public static HashMap<DefaultWeightedEdge, CyEdge> edgeCyEdgeMap2;
}
