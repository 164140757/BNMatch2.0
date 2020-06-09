package Cytoscape.plugin.BNMatch.internal;
/**
 * @author Haotian Bai
 * @Institute CS school,Shanghai University
 */

import Cytoscape.plugin.BNMatch.internal.UI.ControlPanel;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.osgi.framework.BundleContext;

import java.awt.event.ActionEvent;
import java.util.Properties;

// Building a CyActivator for OSGi
public class CyActivator extends AbstractCyActivator {
    public CyActivator() {
        super();
    }


    public void start(BundleContext bc) {

        CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
        CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
        CyTableManager tableManager = getService(bc, CyTableManager.class);
        CyTableFactory tableFactory = getService(bc, CyTableFactory.class);
        CyNetworkNaming namingUtil = getService(bc, CyNetworkNaming.class);
        TaskManager taskManager = getService(bc, TaskManager.class);
        CyLayoutAlgorithmManager layoutAlgorithmManager= getService(bc,CyLayoutAlgorithmManager.class);
        // Add panel
        ControlPanel cyMainPanel = new ControlPanel(networkManager, networkFactory, taskManager, namingUtil);
        Properties props = new Properties();
        // set control panel
        AbstractCyAction loadControlPanelAction = new AbstractCyAction("Load BNMatch") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                registerService(bc, cyMainPanel, CytoPanelComponent.class, new Properties());
            }
        };
        loadControlPanelAction.setPreferredMenu("Apps");
        // register
        registerService(bc, loadControlPanelAction, CyAction.class, new Properties());
        registerService(bc, cyMainPanel, NetworkAddedListener.class, new Properties());
        registerService(bc, cyMainPanel, NetworkAboutToBeDestroyedListener.class, new Properties());

    }

}

