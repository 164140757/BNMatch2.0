package Cytoscape.plugin.PNMatch.internal.Tasks;

import Cytoscape.plugin.PNMatch.internal.UI.InputsAndServices;
import org.cytoscape.model.*;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.*;

public class DisplayTask extends AbstractTask {
    // network table
    private static final String NETWORKS_LIST = "PNMatcherSourceNetwork";
    // node table
    private static final String NETWORK_ID = "NetworkID";
    private static final String NODE_UID = "UID";
    private static final String NODE_NAME = "Name";
    private static final String FROM_NODE = "From";
    private static final String TO_NODE = "To";
    private final CyNetwork idNet;
    private final CyNetwork tgtNet;
    private final CyNetworkNaming namingUtil;
    private final CyNetworkFactory ntf;
    private final CyNetworkViewManager nvm;
    private CyNetwork res;

    public DisplayTask() {
        this.idNet = InputsAndServices.indexNetwork;
        this.tgtNet = InputsAndServices.targetNetwork;
        this.namingUtil = InputsAndServices.naming;
        this.ntf = InputsAndServices.networkFactory;
        this.nvm = InputsAndServices.networkViewManager;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        this.res = constructCombinedNetwork();
    }

    private CyNetwork constructCombinedNetwork(){
        // create networks to show results
        CyNetwork res = ntf.createNetwork();
        res.getRow(res).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("HGA result"));
        CyTable netTable = res.getDefaultNetworkTable();
        netTable.createListColumn(NETWORKS_LIST, String.class, false);
        List<String> srcNameList = new ArrayList<>(Arrays.asList("IndexNetwork","TargetNetwork"));
        netTable.getRow(res.getSUID()).set(NETWORKS_LIST, srcNameList);
        CyTable nodeTable = res.getDefaultNodeTable();
        nodeTable.createColumn(NETWORK_ID, Integer.class, false, -1);
        nodeTable.createColumn(NODE_UID, Long.class, true, -1L);
        nodeTable.createColumn(NODE_NAME, String.class, false, "");
        CyTable edgeTable = res.getDefaultEdgeTable();
        edgeTable.createColumn(NETWORK_ID, String.class, false, "");
        edgeTable.createColumn(FROM_NODE, String.class, false, "");
        edgeTable.createColumn(TO_NODE, String.class, false, "");
        // add network to the result view
        addNetwork(idNet,res,1);
        addNetwork(tgtNet,res,2);
        distributeUIDs(res);
        return res;
    }

    private void addNetwork(CyNetwork net, CyNetwork res,int netID) {
        Map<String, Class> nodeCols = getColumnsToCopy(net.getDefaultNodeTable(), res.getDefaultNodeTable());
        Map<String, Class> edgeCols = getColumnsToCopy(net.getDefaultEdgeTable(), res.getDefaultEdgeTable());
        Map<CyNode, CyNode> old2New = new HashMap<>();
        for(CyNode srcNode : net.getNodeList()){
            CyNode newNode = res.addNode();
            old2New.put(srcNode, newNode);
            CyRow newRow = res.getRow(newNode);
            newRow.set(NETWORK_ID, netID);
            CyRow oldRow = net.getRow(srcNode);
            for(Map.Entry<String, Class> e : nodeCols.entrySet())
                newRow.set(e.getKey(), oldRow.get(e.getKey(), e.getValue()));
        }
        for(CyEdge edge : net.getEdgeList())
        {
            CyEdge newEdge = net.addEdge(old2New.get(edge.getSource()), old2New.get(edge.getTarget()), edge.isDirected());
            CyRow newRow = net.getRow(newEdge);
            newRow.set(NETWORK_ID, netID);
            CyRow oldRow = net.getRow(edge);
            for(Map.Entry<String, Class> e : edgeCols.entrySet())
                newRow.set(e.getKey(), oldRow.get(e.getKey(), e.getValue()));
        }
    }

    private static Map<String, Class> getColumnsToCopy(CyTable src, CyTable dst)
    {
        Map<String, Class> colsToCopy = new HashMap<>();
        for(CyColumn srcCol : src.getColumns())
        {
            CyColumn dstCol = dst.getColumn(srcCol.getName());
            if (dstCol != null && !(dstCol.getType().equals(srcCol.getType())))
            {
                dst.deleteColumn(dstCol.getName());
                dstCol = null;
            }
            VirtualColumnInfo srcColVirtualColumnInfo = srcCol.getVirtualColumnInfo();

            final boolean isVirCol = srcColVirtualColumnInfo != null && srcColVirtualColumnInfo.isVirtual();

            if (dstCol == null && !srcCol.isPrimaryKey())
            {
                if (!isVirCol)
                    dst.createColumn(srcCol.getName(), srcCol.getType(), srcCol.isImmutable(), srcCol.getDefaultValue());
                else
                    dst.addVirtualColumn(srcCol.getName(), srcColVirtualColumnInfo.getSourceColumn(), srcColVirtualColumnInfo.getSourceTable(), srcColVirtualColumnInfo.getTargetJoinKey(), srcCol.isImmutable());
            }

            if(!isVirCol)
                colsToCopy.put(srcCol.getName(), srcCol.getType());
        }
        return colsToCopy;
    }

    public static void distributeUIDs(CyNetwork net)
    {
        CyTable nodeTable = net.getDefaultNodeTable();
        CyColumn col = nodeTable.getColumn(NODE_UID);
        if(col == null)
            nodeTable.createColumn(NODE_UID, Long.class, true, -1L);

        ArrayList<CyNode> todo = new ArrayList<>();
        long maxuid = -1;
        for(CyNode n : net.getNodeList())
        {
            CyRow row = net.getRow(n);
            long uid = row.get(NODE_UID, Long.class, -1L);
            if(uid < 0)
                todo.add(n);
            else
                maxuid = Math.max(uid, maxuid);
        }
        for(CyNode n : todo)
        {
            CyRow row = net.getRow(n);
            row.set(NODE_UID, ++maxuid);
        }
    }

    public CyNetwork getRes() {
        return res;
    }
}
