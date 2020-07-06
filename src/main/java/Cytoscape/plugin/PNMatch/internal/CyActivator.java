package Cytoscape.plugin.PNMatch.internal;
/**
 * @author Haotian Bai
 * @Institute CS school,Shanghai University
 */

import Cytoscape.plugin.PNMatch.internal.Layout.ForceDirectedLayout;
import Cytoscape.plugin.PNMatch.internal.UI.ControlPanel;
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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import java.awt.event.ActionEvent;
import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.TITLE;

// Building a CyActivator for OSGi
public class CyActivator extends AbstractCyActivator {
    public CyActivator() {
        super();
    }


    public void start(BundleContext bc) {

        final CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
        final CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
        final CyTableManager tableManager = getService(bc, CyTableManager.class);
        final CyTableFactory tableFactory = getService(bc, CyTableFactory.class);
        final CyNetworkNaming namingUtil = getService(bc, CyNetworkNaming.class);
        final TaskManager taskManager = getService(bc, TaskManager.class);
        final CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
        final UndoSupport undoSupport = getService(bc, UndoSupport.class);
        final CyNetworkViewManager nvm = getService(bc,CyNetworkViewManager.class);
        // Add panel
        ControlPanel cyMainPanel = new ControlPanel(networkManager, networkFactory, taskManager, namingUtil,registrar,undoSupport,nvm);
        // set control panel
        AbstractCyAction loadControlPanelAction = new AbstractCyAction("Load PNMatcher") {
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

