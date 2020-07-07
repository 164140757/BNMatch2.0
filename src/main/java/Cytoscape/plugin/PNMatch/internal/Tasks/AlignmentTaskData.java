package Cytoscape.plugin.PNMatch.internal.Tasks;

import java.util.HashMap;

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
    public static HashMap<String, String> mapping;
}
