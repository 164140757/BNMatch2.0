package Cytoscape.plugin.BNMatch.internal.util;

import Algorithms.Graph.Network.AdjList;
import Algorithms.Graph.Network.Node;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

import java.util.HashMap;
import java.util.Map;

public class Convert {
    public static AdjList convert(CyNetwork network){
        assert network!=null;
        AdjList out = new AdjList();
        Map<Long, Node> nodeMap = new HashMap<>();
        // get nodes map
        for(CyNode cynode : network.getNodeList()) {
            Node node = new Node(network.getRow(cynode).get(CyNetwork.NAME, String.class));
            nodeMap.put(cynode.getSUID(), node);
        }
        // add edge
        for(CyEdge cyedge : network.getEdgeList()) {
            Node source = nodeMap.get(cyedge.getSource().getSUID());
            Node target = nodeMap.get(cyedge.getTarget().getSUID());
            out.sortAddOneNode(source.getStrName(),target.getStrName());
        }
        return out;
    }
}
