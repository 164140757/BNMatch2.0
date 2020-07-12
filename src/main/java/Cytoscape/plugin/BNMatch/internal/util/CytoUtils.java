package Cytoscape.plugin.BNMatch.internal.util;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;

import java.util.Collection;

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
}
