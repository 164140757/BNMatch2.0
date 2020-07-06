/*
 * @Author: Haotian Bai
 * @Date: 2020-01-07 21:14:12
 * @LastEditTime: 2020-05-07 22:29:02
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \BNMatch2.0\src\main\java\Cytoscape\plugin\BNMatch\internal\AlignTask.java
 */
package Cytoscape.plugin.PNMatch.internal.Tasks;

import Algorithms.Graph.HGA.HGA;
import Algorithms.Graph.Network.Node;
import Algorithms.Graph.Utils.AdjList.DirectedGraph;
import Algorithms.Graph.Utils.AdjList.UndirectedGraph;
import Algorithms.Graph.Utils.SimMat;
import Cytoscape.plugin.PNMatch.internal.UI.Parameters;
import IO.GraphFileReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class HGATask extends AbstractTask {

	private final Parameters params;
	private final CyNetworkFactory ntf;
	public double EC;
	public double ES;
	public double PE;
	public double PS;
	public double score;
	public HashMap<String, String> mapping;
	public CyNetwork indexNetwork;

	/**
	 * Create new AlignTask instance.
	 * @param params Parameters for the instance.
	 */
	public HGATask(CyNetworkFactory networkFactory,Parameters params) {
		this.params = params;
		this.ntf  = networkFactory;
	}
	
	public void run(TaskMonitor monitor) {
		// check if there's no networks input
		if(params.indexNetwork == null || params.targetNetwork == null){
			throw new RuntimeException("Both index-network and target-network should be selected.");
		}
		monitor.setTitle("HGA mapping");
		// HGA
		try {
			HGARun();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	private void HGARun() throws IOException {
		GraphFileReader reader = new GraphFileReader(true, false, false);
		indexNetwork = params.indexNetwork;
		CyNetwork targetNetwork = params.targetNetwork;
		File simMatFile = params.simMatFile;
		UndirectedGraph indNet = convert(indexNetwork);
		UndirectedGraph tgtNet = convert(targetNetwork);
		SimMat simMat = reader.readToSimMat(simMatFile,indNet.getAllNodes(),tgtNet.getAllNodes(),true);
//		hVal, tolerance, bio-factor
		Vector<Double> p = params.params;
		HGA hga = new HGA(simMat, indNet, tgtNet,p.get(2),params.force,p.get(0),p.get(1));
		hga.debugOut = false;
		hga.log = false;
		hga.run();
		// score
		EC = hga.getEC_res();
		ES = hga.getES_res();
		PE = hga.getPE_res();
		PS = hga.getPS_res();
		score = hga.getScore_res();
		mapping = hga.getMappingResult();
	}


	public static UndirectedGraph convert(CyNetwork network){
		assert network!=null;
		UndirectedGraph out = new UndirectedGraph();
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
