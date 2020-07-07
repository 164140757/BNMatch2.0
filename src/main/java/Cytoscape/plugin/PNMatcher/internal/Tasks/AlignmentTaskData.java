package Cytoscape.plugin.PNMatcher.internal.Tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.util.HashMap;
import java.util.HashSet;

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
    public static HashMap<CyNode, CyNode> mapping;
    // result to display
    public static CyNetwork combinedNet;
    // left nodes in the target network without being mapped
    public static HashSet<CyNode> leftTgtNodes;
}
