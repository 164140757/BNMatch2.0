package Cytoscape.plugin.PNMatch.internal.Tasks;

import Cytoscape.plugin.PNMatch.internal.Layout.ForceDirectedLayout;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import java.util.Collection;

public class DisplayTask extends AbstractTask {
    // network table
    private static final String NETWORK_LIST = "PNMatcherSourceNetwork";
    // node table
    private static final String NETWORK_ID = "NetworkID";
    private static final String NODE_UID = "UID";
    private static final String NODE_NAME = "Name";
    private static final String NETWORK_FROM = "Belong_to";
    private static final String FROM_NODE = "From";
    private static final String TO_NODE = "To";
    private final ForceDirectedLayout fdLayout;
    private final CyNetwork idNet;
    private final CyNetworkNaming namingUtil;
    private final CyNetworkFactory ntf;
    private final CyNetworkViewManager vmg;
    private final CyNetworkViewManager nvm;
    private final CyServiceRegistrar rgs;

    public DisplayTask(CyServiceRegistrar rgs, UndoSupport uds, CyNetworkNaming namingUtil, CyNetworkFactory ntf,CyNetworkViewManager nvm, HGATask hgaTask) {
        this.rgs = rgs;
        this.fdLayout = new ForceDirectedLayout(rgs,uds);
        this.idNet = hgaTask.indexNetwork;
        this.namingUtil = namingUtil;
        this.ntf = ntf;
        this.nvm = nvm;
        vmg = null;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {

        Collection<CyNetworkView> idNetView = nvm.getNetworkViews(idNet);
    }

    private CyNetwork constructCombinedNetwork(){
        CyNetworkFactory nf = rgs.getService(CyNetworkFactory.class);
        // create networks to show results
        CyNetwork res = ntf.createNetwork();
        res.getRow(res).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("HGA result"));
        CyTable netTable = res.getDefaultNetworkTable();
        netTable.createListColumn(NETWORK_LIST, String.class, false);
        CyTable nodeTable = res.getDefaultNodeTable();
        nodeTable.createColumn(NETWORK_ID, Integer.class, false, -1);
        nodeTable.createColumn(NODE_UID, Long.class, true, -1L);
        nodeTable.createColumn(NODE_NAME, String.class, false, "");
        nodeTable.createColumn(NETWORK_FROM, String.class, false, "");
        CyTable edgeTable = res.getDefaultEdgeTable();
        edgeTable.createColumn(FROM_NODE, String.class, false, "");
        edgeTable.createColumn(TO_NODE, String.class, false, "");
        edgeTable.createColumn(NETWORK_FROM, String.class, false, "");

        
    }
}
