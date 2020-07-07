package Cytoscape.plugin.PNMatcher.internal.Tasks;

import Cytoscape.plugin.PNMatcher.internal.UI.InputsAndServices;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class PairLayoutTask extends AbstractTask {

    private CyNetworkManager networkManager;
    private CyNetworkViewFactory viewFactory;
    private CyLayoutAlgorithmManager layoutAlgorithmManager;
    private CyEventHelper eventHelper;

    PairLayoutTask(){

    }
    @Override
    public void run(TaskMonitor taskMonitor) {
        networkManager = InputsAndServices.networkManager;
        viewFactory = InputsAndServices.networkViewFactory;
        layoutAlgorithmManager = InputsAndServices.layoutAlgorithmManager;
        eventHelper = InputsAndServices.eventHelper;
        //pair layout setting
        CyNetwork combinedNet = AlignmentTaskData.combinedNet;
        networkManager.addNetwork(combinedNet);
        CyNetworkView view = viewFactory.createNetworkView(combinedNet);
        CyLayoutAlgorithm alg = layoutAlgorithmManager.getLayout("force-directed");
        createPair(view,alg);

    }

    private void createPair(CyNetworkView view, CyLayoutAlgorithm alg) {
        HashMap<CyNode, CyNode> mapping = AlignmentTaskData.mapping;
        HashSet<CyNode> leftNodes = AlignmentTaskData.leftTgtNodes;
        HashMap<CyNode, CyNode> old2new = new HashMap<>();
        eventHelper.flushPayloadEvents();
        Collection<View<CyNode>> nodes = view.getNodeViews();

    }
}
