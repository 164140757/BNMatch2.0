package Cytoscape.plugin.PNMatch.internal.Layout;

import Cytoscape.plugin.PNMatch.internal.Layout.ForceDirectedLayout;
import Cytoscape.plugin.PNMatch.internal.Layout.ForceDirectedLayoutContext;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.util.force.SpringForce;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.AbstractLayoutTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import java.util.*;

/**
 * This class contains all the logic corresponding to the prefuse.util.force-directed aspects of boundary layout
 */
public class ForceDirectedLayoutTask extends AbstractLayoutTask {
	private ForceDirectedLayout.Integrators integrator;
	private Map<CyNode, ForceItem> forceItems;
	private ForceDirectedLayoutContext context;
	private CyServiceRegistrar registrar;
	private final List<View<CyNode>> nodeViewList;
	private final List<View<CyEdge>> edgeViewList;
	private final String chosenCategory;
	final CyNetworkView netView;
	/**
	 * Construct a prefuse.util.force directed layout task, holding the information relevant to the
	 * network view including: nodes, edges, boundaries.
	 * @param displayName is the name of the algorithm
	 * @param netView is the network view on which the user has chosen to execute the algorithm
	 * @param nodesToLayOut is the list of nodes which are to be layed out
	 * @param context is the context of this execution which the user has tuned or is the default context
	 * @param layoutAttribute is the attribute by which the user chose to run the algorithm
	 * @param integrator is the prefuse.util.force integrator to be used for the prefuse.util.force-directed layout
	 * @param registrar provides the services used by this task including various managers
	 * @param undo allows the user to undo this layout
	 */
	public ForceDirectedLayoutTask(final String displayName, final CyNetworkView netView,
			final Set<View<CyNode>> nodesToLayOut, final ForceDirectedLayoutContext context,
			final String layoutAttribute, final ForceDirectedLayout.Integrators integrator,
			final CyServiceRegistrar registrar, final UndoSupport undo) {
		super(displayName, netView, nodesToLayOut, layoutAttribute, undo);

		if (nodesToLayOut.size() > 0)
			nodeViewList = new ArrayList<>(nodesToLayOut);
		else
			nodeViewList = new ArrayList<>(netView.getNodeViews());

		edgeViewList = new ArrayList<>(netView.getEdgeViews());

		this.netView = netView;
		this.context = context;
		this.integrator = integrator;
		this.registrar = registrar;
		this.chosenCategory = layoutAttribute;

		// We don't want to recenter or we'll move all of our nodes away from their boundaries
//		recenter = false; // This is provided by AbstractLayoutTask
		forceItems = new HashMap<>();
	}

	/**
	 * The layout initializes nodes to their respective boundaries and runs a prefuse.util.force-directed
	 * layout consisting of a variety of forces: 
	 * 1) NBody prefuse.util.force: this repulsive prefuse.util.force pushes nearby nodes away from each other and when enabled,
	 * handles avoiding node overlapping
	 * 2) Spring prefuse.util.force: this prefuse.util.force acts as a spring with a desired length
	 * 3) Drag prefuse.util.force: this prefuse.util.force slows nodes down so they do not move very quickly
	 */
	@Override
	protected void doLayout(TaskMonitor taskMonitor) {

		//initialize simulation and add the various forces
		ForceSimulator m_fsim = new ForceSimulator();
		m_fsim.speedLimit = context.speedLimit;
		m_fsim.addForce(new SpringForce());
		forceItems.clear();

		// initialize node locations and properties
		for (View<CyNode> nodeView : nodeViewList) {
			ForceItem fitem = forceItems.get(nodeView.getModel()); 
			if (fitem == null) {
				fitem = new ForceItem();
				forceItems.put(nodeView.getModel(), fitem);
			}

			fitem.mass = (float) context.defaultNodeMass;

			Object group = null;
			if(chosenCategory != null)
				group = netView.getModel().getRow(nodeView.getModel()).getRaw(chosenCategory);

			double width = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
			double height = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
			fitem.dimensions[0] = (float) width;
			fitem.dimensions[1] = (float) height;
			fitem.category = group;

			m_fsim.addItem(fitem);
		}


		//initialize edges
		for (View<CyEdge> edgeView : edgeViewList) {
			CyEdge edge = edgeView.getModel();
			CyNode n1 = edge.getSource();
			ForceItem f1 = forceItems.get(n1); 
			CyNode n2 = edge.getTarget();
			ForceItem f2 = forceItems.get(n2); 
			if ( f1 == null || f2 == null )
				continue;
			m_fsim.addSpring(f1, f2, (float) context.defaultSpringCoefficient, (float) context.defaultSpringLength); 
		}

		// perform layout
		long timestep = 1000L;
		m_fsim.speedLimit = 2f;
		for (int i = 0; i < context.numIterations / 3 && !cancelled; i++) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep + 50;
			m_fsim.runSimulator(step);
			taskMonitor.setProgress((int)(((double)i/(double)context.numIterations)*90.+5));
		}

		// perform layout at desired speedlimit with boundary forces
		m_fsim.speedLimit = context.speedLimit;
		for (int i = context.numIterations / 3; i < 2 * context.numIterations / 3 && !cancelled; i++) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep + 50;
			m_fsim.runSimulator(step);
			taskMonitor.setProgress((int)(((double)i/(double)context.numIterations)*90.+5));
		}

		// perform layout while looking at NBodyForce interactions
		m_fsim.addForce(new NBodyForce(context.avoidOverlap));
		for(int i = 2 * context.numIterations / 3; i < context.numIterations && !cancelled; i++) {
			timestep *= (1.0 - i/(double)context.numIterations);
			long step = timestep + 50;
			m_fsim.runSimulator(step);
			taskMonitor.setProgress((int)(((double)i/(double)context.numIterations)*90.+5));
		}
		updateNodeViews();
	}

	/** Private method
	 * Update positions of nodes
	 */
	private void updateNodeViews() {
		for (CyNode node : forceItems.keySet()) {
			ForceItem fitem = forceItems.get(node); 
			View<CyNode> nodeView = netView.getNodeView(node);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, (double) fitem.location[0]);
			nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, (double) fitem.location[1]);
		}
	}


}
