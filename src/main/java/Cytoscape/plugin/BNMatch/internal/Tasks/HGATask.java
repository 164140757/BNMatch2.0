/*
 * @Author: Haotian Bai
 * @Date: 2020-01-07 21:14:12
 * @LastEditTime: 2020-05-07 22:29:02
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \BNMatch.0\src\main\java\Cytoscape\plugin\BNMatch\internal\AlignTask.java
 */
package Cytoscape.plugin.BNMatch.internal.Tasks;


import Algorithms.Graph.HGA.HGA;
import Cytoscape.plugin.BNMatch.internal.UI.InputsAndServices;
import DS.Matrix.SimMat;
import DS.Network.UndirectedGraph;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;


public class HGATask extends AbstractTask {

    private final CyNetworkFactory ntf;

    /**
     * Create new AlignTask instance.
     */
    public HGATask() {
        this.ntf = InputsAndServices.networkFactory;
    }

    public void run(TaskMonitor monitor) throws IOException {
        monitor.setStatusMessage("HGA mapping");
        SimMat<String> simMat = InputsAndServices.siMat;
        UndirectedGraph<String, DefaultWeightedEdge> indNet = InputsAndServices.indNet;
        UndirectedGraph<String, DefaultWeightedEdge> tgtNet = InputsAndServices.tgtNet;
        HGA.debugOut = false;
        HGA<String, DefaultWeightedEdge> hga = new HGA<>(simMat, indNet, tgtNet,
                InputsAndServices.bF, InputsAndServices.force,
                InputsAndServices.hVal, InputsAndServices.tol);
        // HGA
        if (!InputsAndServices.onlyDisplay) {
            HGA.GPU = InputsAndServices.GPU;
            hga.run();
            // score
            AlignmentTaskData.EC = hga.getEC_res();
            AlignmentTaskData.ES = hga.getES_res();
            AlignmentTaskData.PE = hga.getPE_res();
            AlignmentTaskData.PS = hga.getPS_res();
            AlignmentTaskData.score = hga.getScore_res();
            setupMapping(hga.getMappingResult());
            // mapping edges
            AlignmentTaskData.mappingEdges = hga.getMappingEdges();
            InputsAndServices.logger.info("Mapping finished!After " + hga.getIter_res() + "times. " + "\nEC = " + hga.getEC_res() +
                    ", ES = " + hga.getES() + ", PE = " + hga.getPE() + ", PS = " + hga.getPS_res() + ", Total score = "
                    + hga.getScore());
        } else {
            hga.setUdG1(InputsAndServices.indNet);
            hga.setUdG2(InputsAndServices.tgtNet);
            hga.setEC(InputsAndServices.mapping);
            AlignmentTaskData.EC = hga.getEC();
            setupMapping(InputsAndServices.mapping);
            // mapping edges
            AlignmentTaskData.mappingEdges = hga.getMappingEdges();
            InputsAndServices.logger.info("Load your mapping and the Edge correctness is:" + "\nEC = " + hga.getEC());
        }

    }


    private void setupMapping(HashMap<String, String> mappingResult) {
        // name to UID
        Map<String, String> mapInverse = mappingResult.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
        // save
        AlignmentTaskData.mapping = mappingResult;
        AlignmentTaskData.inverseMapping = mapInverse;
    }

    private HashMap<String, Long> getNameUIDMap(CyNetwork network) {
        HashMap<String, Long> res = new HashMap<>();
        network.getNodeList().forEach(cyNode -> res.put(network.getRow(cyNode).get(CyNetwork.NAME, String.class), cyNode.getSUID()));
        return res;
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

