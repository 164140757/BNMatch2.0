package Cytoscape.plugin.PNMatch.internal.UI;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;

import java.io.File;

public final class InputsAndServices {

    public static final boolean force = true;
    public static CyNetwork indexNetwork;
    public static CyNetwork targetNetwork;
    public static File simMatFile;
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

    public static void initServices() {
        if(registrar!=null){
            networkFactory = registrar.getService(CyNetworkFactory.class);
            networkManager = registrar.getService(CyNetworkManager.class);
            naming = registrar.getService(CyNetworkNaming.class);
            taskManager = registrar.getService(TaskManager.class);
            undoSupport = registrar.getService(UndoSupport.class);
            networkViewManager = registrar.getService(CyNetworkViewManager.class);
        }
    }
}
