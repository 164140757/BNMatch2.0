package Cytoscape.plugin.BNMatch.internal.UI;

import DS.Matrix.SimMat;
import DS.Network.UndirectedGraph;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.swing.text.DefaultEditorKit;
import java.io.File;
import java.util.HashMap;

public final class InputsAndServices {

    public static boolean force;
    public static CyNetwork indexNetwork;
    public static CyNetwork targetNetwork;
    public static File InputFile;
    // hungarian allocation account
    public static double hVal;
    // tolerance for the similarity to converge
    public static double tol;
    // bio-factor percentage
    public static double bF;

    // Cytoscape services
    public static CyServiceRegistrar registrar;
    public static CyNetworkManager networkManager;
    public static CyNetworkFactory networkFactory;
    public static CyNetworkNaming naming;
    public static TaskManager taskManager;
    public static UndoSupport undoSupport;
    public static CyNetworkViewManager networkViewManager;
    public static CyNetworkViewFactory networkViewFactory;
    public static CyLayoutAlgorithmManager layoutAlgorithmManager;
    public static CyEventHelper eventHelper;
    public static VisualMappingFunctionFactory mapFactoryService;
    public static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
    public static SimMat<String> siMat;
    public static UndirectedGraph<String, DefaultWeightedEdge> indNet;
    public static UndirectedGraph<String, DefaultWeightedEdge> tgtNet;
    public static boolean onlyDisplay;
    public static boolean GPU;
    public static HashMap<String, String> mapping;

    public static void initServices() {
        if(registrar!=null){
            networkFactory = registrar.getService(CyNetworkFactory.class);
            networkManager = registrar.getService(CyNetworkManager.class);
            naming = registrar.getService(CyNetworkNaming.class);
            taskManager = registrar.getService(TaskManager.class);
            undoSupport = registrar.getService(UndoSupport.class);
            networkViewManager = registrar.getService(CyNetworkViewManager.class);
            networkViewFactory = registrar.getService(CyNetworkViewFactory.class);
            layoutAlgorithmManager = registrar.getService(CyLayoutAlgorithmManager.class);
            eventHelper = registrar.getService(CyEventHelper.class);
            mapFactoryService = registrar.getService(VisualMappingFunctionFactory.class,"(mapping.type=continuous)");
        }
    }
}
