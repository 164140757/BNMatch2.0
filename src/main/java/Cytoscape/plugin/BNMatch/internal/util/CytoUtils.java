package Cytoscape.plugin.BNMatch.internal.util;

import DS.Network.UndirectedGraph;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CytoUtils {
    public static CyNetworkView getCyNetworkView(CyNetwork network, CyNetworkViewManager viewManager, CyNetworkViewFactory viewFactory) {
        final Collection<CyNetworkView> views = viewManager.getNetworkViews(network);
        CyNetworkView view = null;
        if(views.size() != 0)
            view = views.iterator().next();

        if (view == null) {
            // create a new view for my network
            view = viewFactory.createNetworkView(network);
        } else {
            System.out.println("networkView already existed.");
        }
        return view;
    }
    public static UndirectedGraph<String, DefaultWeightedEdge> convert(CyNetwork network) {
        assert network != null;
        UndirectedGraph<String, DefaultWeightedEdge> out = new UndirectedGraph<>(DefaultWeightedEdge.class);
        Map<Long, String> nodeMap = new HashMap<>();
        // get nodes map
        for (CyNode cynode : network.getNodeList()) {
            String nName = network.getRow(cynode).get(CyNetwork.NAME, String.class);
            nodeMap.put(cynode.getSUID(), nName);
        }
        // add edge
        for (CyEdge cyedge : network.getEdgeList()) {
            String source = nodeMap.get(cyedge.getSource().getSUID());
            String target = nodeMap.get(cyedge.getTarget().getSUID());
            out.addVertex(source);
            out.addVertex(target);
            out.addEdge(source, target);
        }
        return out;
    }
}
