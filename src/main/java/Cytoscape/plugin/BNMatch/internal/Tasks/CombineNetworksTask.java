package Cytoscape.plugin.BNMatch.internal.Tasks;

import Cytoscape.plugin.BNMatch.internal.UI.InputsAndServices;
import org.cytoscape.model.*;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;

public class CombineNetworksTask extends AbstractTask {
    // network table
    private static final String NETWORKS_LIST = "BNMatchSourceNetwork";
    // node table
    private static final String NETWORK_ID = "NetworkID";
    private static final String NODE_UID = "UID";
    private static final String PAIRED_NODE = "PairedNode";
    private final CyNetwork idNet;
    private final CyNetwork tgtNet;
    private final CyNetworkNaming namingUtil;
    private final CyNetworkFactory ntf;
    private HashMap<CyNode, CyNode> old2NewIndex;
    private HashMap<CyNode, CyNode> old2NewTarget;

    public CombineNetworksTask() {
        this.idNet = InputsAndServices.indexNetwork;
        this.tgtNet = InputsAndServices.targetNetwork;
        this.namingUtil = InputsAndServices.naming;
        this.ntf = InputsAndServices.networkFactory;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setStatusMessage("setting up result view...");
        AlignmentTaskData.combinedNet = constructCombinedNetwork();
    }


    private CyNetwork constructCombinedNetwork() {
        // create networks to show results
        CyNetwork res = ntf.createNetwork();
        String index = InputsAndServices.indexNetwork.getRow(InputsAndServices.indexNetwork).get(CyNetwork.NAME, String.class);
        String target = InputsAndServices.targetNetwork.getRow(InputsAndServices.targetNetwork).get(CyNetwork.NAME, String.class);
        res.getRow(res).set(CyNetwork.NAME, namingUtil.getSuggestedNetworkTitle("BNMatch result[" + index + "," + target + "]"));
        CyTable netTable = res.getDefaultNetworkTable();
        netTable.createListColumn(NETWORKS_LIST, String.class, false);
        List<String> srcNameList = new ArrayList<>(Arrays.asList("IndexNetwork", "TargetNetwork"));
        netTable.getRow(res.getSUID()).set(NETWORKS_LIST, srcNameList);
        CyTable nodeTable = res.getDefaultNodeTable();
        nodeTable.createColumn(PAIRED_NODE, String.class, false, "");
        nodeTable.createColumn(NETWORK_ID, Integer.class, false, -1);
        nodeTable.createColumn(NODE_UID, Long.class, true, -1L);
        CyTable edgeTable = res.getDefaultEdgeTable();
        edgeTable.createColumn(NETWORK_ID, Integer.class, false, -1);
        // add network to the result view
        old2NewIndex = new HashMap<>();
        old2NewTarget = new HashMap<>();
        addNetwork(idNet, res, 1);
        addNetwork(tgtNet, res, 2);
        distributeUIDs(res);
        setupMap(res);
        return res;
    }

    private void addNetwork(CyNetwork net, CyNetwork res, int netID) {
        HashMap<String, String> mapping = AlignmentTaskData.mapping;
        Map<String, String> inverseMapping = AlignmentTaskData.inverseMapping;
        Map<String, Class> nodeCols = getColumnsToCopy(net.getDefaultNodeTable(), res.getDefaultNodeTable());
        Map<String, Class> edgeCols = getColumnsToCopy(net.getDefaultEdgeTable(), res.getDefaultEdgeTable());
        for (CyNode srcNode : net.getNodeList()) {
            String strNode = net.getRow(srcNode).get(CyNetwork.NAME, String.class);
//            CyNode newNode = res.addNode();
//            CyRow newRow = res.getRow(newNode);
            CyNode newNode = null;
            CyRow newRow = null;
            if (netID == 1) {
                newNode = res.addNode();
                newRow = res.getRow(newNode);
                newRow.set(NETWORK_ID, netID);
                newRow.set("name", strNode);
                old2NewIndex.put(srcNode, newNode);

            } else if (netID == 2 && mapping.containsValue(strNode)) {
                // only nodes that mapped
                newNode = res.addNode();
                newRow = res.getRow(newNode);
                newRow.set(NETWORK_ID, netID);
                newRow.set("name", strNode);
                old2NewTarget.put(srcNode, newNode);
                newRow.set(PAIRED_NODE, inverseMapping.get(strNode));

            }
            if(newNode!=null){
                newRow.set(PAIRED_NODE, mapping.get(strNode));
                CyRow oldRow = net.getRow(srcNode);
                for (Map.Entry<String, Class> e : nodeCols.entrySet()) {
                    String key = e.getKey();
                    newRow.set(key, oldRow.get(key, e.getValue()));
                }
            }
        }

        for (CyEdge edge : net.getEdgeList()) {
            CyEdge newEdge = null;
            if (netID == 1) {
                newEdge = res.addEdge(
                        old2NewIndex.get(edge.getSource()),
                        old2NewIndex.get(edge.getTarget()),
                        edge.isDirected());
            } else if (netID == 2) {
                boolean b1 = old2NewTarget.containsKey(edge.getSource());
                boolean b2 = old2NewTarget.containsKey(edge.getTarget());
                if(b1&&b2){
                    newEdge = res.addEdge(
                            old2NewTarget.get(edge.getSource()),
                            old2NewTarget.get(edge.getTarget()),
                            edge.isDirected());
                }
            }
            if(newEdge!=null){
                CyRow newRow = res.getRow(newEdge);
                newRow.set(NETWORK_ID, netID);
                CyRow oldRow = net.getRow(edge);
                for (Map.Entry<String, Class> e : edgeCols.entrySet()) {
                    newRow.set(e.getKey(), oldRow.get(e.getKey(), e.getValue()));
                }
            }
        }
    }

    private void setupMap(CyNetwork res) {
        // nodes
        HashMap<String, String> mapping = AlignmentTaskData.mapping;
        AlignmentTaskData.cyNodeMapping = new HashMap<>();
        HashMap<String, CyNode> tgtMap = new HashMap<>();
        tgtNet.getNodeList().forEach(n -> {
            String strNode = tgtNet.getRow(n).get(CyNetwork.NAME, String.class);
            tgtMap.put(strNode, old2NewTarget.get(n));
        });

        idNet.getNodeList().forEach(
                n -> {
                    String strNode = idNet.getRow(n).get(CyNetwork.NAME, String.class);
                    CyNode tgtNode = tgtMap.get(mapping.get(strNode));
                    AlignmentTaskData.cyNodeMapping.put(old2NewIndex.get(n), tgtNode);
                }
        );
        // edges
        HashMap<DefaultWeightedEdge, CyEdge> map1 = new HashMap<>();
        HashMap<DefaultWeightedEdge, CyEdge> map2 = new HashMap<>();
        res.getEdgeList().forEach(
                cyEdge -> {
                    CyNode n1 = cyEdge.getSource();
                    CyNode n2 = cyEdge.getTarget();
                    String str1 = res.getRow(n1).get(CyNetwork.NAME, String.class);
                    String str2 = res.getRow(n2).get(CyNetwork.NAME, String.class);
                    int netNum1 = res.getRow(n1).get(NETWORK_ID, Integer.class);
                    int netNum2 = res.getRow(n2).get(NETWORK_ID, Integer.class);
                    if (netNum1 == 1 && netNum2 == 1) {
                        map1.put(InputsAndServices.indNet.getEdge(str1,str2), cyEdge);
                    }
                    if (netNum1 == 2 && netNum2 == 2) {
                        map2.put(InputsAndServices.tgtNet.getEdge(str1,str2), cyEdge);
                    }
                }
        );
        AlignmentTaskData.edgeCyEdgeMap1 = map1;
        AlignmentTaskData.edgeCyEdgeMap2 = map2;

        AlignmentTaskData.indexOldToNew = old2NewIndex;
        AlignmentTaskData.targetOldToNew = old2NewTarget;
    }


    private static Map<String, Class> getColumnsToCopy(CyTable src, CyTable dst) {
        Map<String, Class> colsToCopy = new HashMap<>();
        for (CyColumn srcCol : src.getColumns()) {
            CyColumn dstCol = dst.getColumn(srcCol.getName());
            if (dstCol != null && !(dstCol.getType().equals(srcCol.getType()))) {
                dst.deleteColumn(dstCol.getName());
                dstCol = null;
            }
            VirtualColumnInfo srcColVirtualColumnInfo = srcCol.getVirtualColumnInfo();

            final boolean isVirCol = srcColVirtualColumnInfo != null && srcColVirtualColumnInfo.isVirtual();

            if (dstCol == null && !srcCol.isPrimaryKey()) {
                if (!isVirCol)
                    dst.createColumn(srcCol.getName(), srcCol.getType(), srcCol.isImmutable(), srcCol.getDefaultValue());
                else
                    dst.addVirtualColumn(srcCol.getName(), srcColVirtualColumnInfo.getSourceColumn(), srcColVirtualColumnInfo.getSourceTable(), srcColVirtualColumnInfo.getTargetJoinKey(), srcCol.isImmutable());
            }

            if (!isVirCol)
                colsToCopy.put(srcCol.getName(), srcCol.getType());
        }
        return colsToCopy;
    }

    public static void distributeUIDs(CyNetwork net) {
        CyTable nodeTable = net.getDefaultNodeTable();
        CyColumn col = nodeTable.getColumn(NODE_UID);
        if (col == null)
            nodeTable.createColumn(NODE_UID, Long.class, true, -1L);

        ArrayList<CyNode> todo = new ArrayList<>();
        long maxuid = -1;
        for (CyNode n : net.getNodeList()) {
            CyRow row = net.getRow(n);
            long uid = row.get(NODE_UID, Long.class, -1L);
            if (uid < 0)
                todo.add(n);
            else
                maxuid = Math.max(uid, maxuid);
        }
        for (CyNode n : todo) {
            CyRow row = net.getRow(n);
            row.set(NODE_UID, ++maxuid);
        }
    }

}
