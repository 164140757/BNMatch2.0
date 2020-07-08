/*
 * @Author: Haotian Bai
 * @Date: 2020-01-07 21:14:12
 * @LastEditTime: 2020-05-07 22:29:02
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \BNMatch2.0\src\main\java\Cytoscape\plugin\BNMatch\internal\AlignTask.java
 */
package Cytoscape.plugin.PNMatcher.internal.Tasks;


import Cytoscape.plugin.PNMatcher.internal.UI.InputsAndServices;
import Internal.Algorithms.Graph.HGA.HGA;
import Internal.Algorithms.Graph.Network.Node;
import Internal.Algorithms.Graph.Utils.AdjList.DirectedGraph;
import Internal.Algorithms.Graph.Utils.AdjList.UndirectedGraph;
import Internal.Algorithms.Graph.Utils.SimMat;
import Internal.Algorithms.IO.GraphFileReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class HGATask extends AbstractTask {

	private final CyNetworkFactory ntf;

	/**
	 * Create new AlignTask instance.
	 */
	public HGATask() {
		this.ntf  = InputsAndServices.networkFactory;
	}
	
	public void run(TaskMonitor monitor) {
		monitor.setStatusMessage("HGA mapping");
		// HGA
		try {
			HGARun();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void HGARun() throws IOException {

		GraphFileReader reader = new GraphFileReader(true, false, false);
		CyNetwork indexNetwork = InputsAndServices.indexNetwork;
		CyNetwork targetNetwork = InputsAndServices.targetNetwork;
		File simMatFile = InputsAndServices.simMatFile;
		UndirectedGraph indNet = convert(indexNetwork);
		UndirectedGraph tgtNet = convert(targetNetwork);
		SimMat simMat = reader.readToSimMat(simMatFile,indNet.getAllNodes(),tgtNet.getAllNodes(),true);
		HGA.debugOut = false;
		HGA.log = false;
		HGA hga = new HGA(simMat, indNet, tgtNet, InputsAndServices.bF, InputsAndServices.force, InputsAndServices.hVal, InputsAndServices.tol);

		hga.run();
		// score
		AlignmentTaskData.EC = hga.getEC_res();
		AlignmentTaskData.ES = hga.getES_res();
		AlignmentTaskData.PE = hga.getPE_res();
		AlignmentTaskData.PS = hga.getPS_res();
		AlignmentTaskData.score = hga.getScore_res();
		setupMapping(hga.getMappingResult());
	}

	private void setupMapping( HashMap<String, String> mappingResult) {
		// name to UID
		Map<String, String> mapInverse = mappingResult.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue,Map.Entry::getKey));
		// save
		AlignmentTaskData.mapping = mappingResult;
		AlignmentTaskData.inverseMapping = mapInverse;
	}

	private HashMap<String, Long> getNameUIDMap(CyNetwork network) {
		HashMap<String,Long> res = new HashMap<>();
		network.getNodeList().forEach(cyNode -> res.put(network.getRow(cyNode).get(CyNetwork.NAME, String.class),cyNode.getSUID()));
		return res;
	}


	public static UndirectedGraph convert(CyNetwork network){
		assert network!=null;
		UndirectedGraph out = new UndirectedGraph();
		Map<Long, Node> nodeMap = new HashMap<>();
		// get nodes map
		for(CyNode cynode : network.getNodeList()) {
			String nName = network.getRow(cynode).get(CyNetwork.NAME, String.class);
			Node node = new Node(nName);
			nodeMap.put(cynode.getSUID(), node);
		}
		// add edge
		for(CyEdge cyedge : network.getEdgeList()) {
			Node source = nodeMap.get(cyedge.getSource().getSUID());
			Node target = nodeMap.get(cyedge.getTarget().getSUID());
			out.addOneNode(source.getStrName(),target.getStrName(),0);
		}
		return out;
	}
	public CyNetwork convert(UndirectedGraph network){
		assert network!=null;
		DirectedGraph graph = network.toDirect();
		CyNetwork out = ntf.createNetwork();
		graph.parallelStream().forEach(h-> h.forEach(n->{
			CyNode node1 = out.addNode();
			CyNode node2 = out.addNode();
			out.getDefaultNodeTable().getRow(node1.getSUID()).set("name", h.signName);
			out.getDefaultNodeTable().getRow(node2.getSUID()).set("name", n.getStrName());
			out.addEdge(node1,node2,false);
		}));
		return out;
	}

}

