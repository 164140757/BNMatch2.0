/*
 * @Author: Haotian Bai
 * @Date: 2020-01-07 21:14:12
 * @LastEditTime: 2020-05-07 22:29:02
 * @LastEditors: Please set LastEditors
 * @Description: In User Settings Edit
 * @FilePath: \BNMatch2.0\src\main\java\Cytoscape\plugin\BNMatch\internal\AlignTask.java
 */
package Cytoscape.plugin.BNMatch.internal;

import Cytoscape.plugin.BNMatch.internal.UI.Parameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class AlignTask extends AbstractTask {

	private final CyNetworkFactory networkFactory;
	private final CyNetworkManager networkManager;
	private final Parameters params;
	private CyNetwork result;
	private final CyNetworkNaming namingUtil;

	/**
	 * Create new AlignTask instance.
	 * @param networkFactory CyNetworkFactory instance of creating the result.
	 * @param networkManager CyNetworkManager instance of adding the results to the network collection.
	 * @param namingUtil change the result network name.
	 * @param params Parameters for the instance.
	 */
	public AlignTask(CyNetworkFactory networkFactory, CyNetworkManager networkManager, Parameters params,final CyNetworkNaming namingUtil) {
		this.networkFactory = networkFactory;
		this.networkManager = networkManager;
		this.params = params;
		this.result = null;
		this.namingUtil = namingUtil;
	}
	
	public void run(TaskMonitor monitor) {
		// check if there's no networks input
		if(params.indexNetwork == null || params.targetNetwork == null){
			throw new RuntimeException("Both index-network and target-network should be selected.");
		}

		// Create an empty network
		CyNetwork myNet = networkFactory.createNetwork();
		myNet.getRow(myNet).set(CyNetwork.NAME,
				      namingUtil.getSuggestedNetworkTitle("Result"));
		// Create a network for output
		CyNetwork result = networkFactory.createNetwork();
		result.getRow(result).set(CyNetwork.NAME,
				namingUtil.getSuggestedNetworkTitle("BNMatch alignment result"));
//		// HGA
//		CyNetwork indexNetwork = params.indexNetwork;
//		CyNetwork targetNetwork = params.targetNetwork;
//		AdjList indexAdjList = convert(indexNetwork);
//		AdjList targetAdjList = convert(targetNetwork);


	}



}
