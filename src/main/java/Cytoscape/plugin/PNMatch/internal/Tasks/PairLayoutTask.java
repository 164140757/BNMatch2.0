package Cytoscape.plugin.PNMatch.internal.Tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.LayoutEdit;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import java.util.HashSet;
import java.util.Set;

public class PairLayoutTask extends AbstractTask {
    private final CyNetworkView view;
    private final UndoSupport undo;
    private CyNetwork net;
    private CyLayoutAlgorithm layout;
    private Object layoutContext;
    private String layoutField;
    private Set<CyNode> selectedNodes;

    public PairLayoutTask(CyNetworkView cyView, CyLayoutAlgorithm layout, Object layoutContext, Set<View<CyNode>> views, String layoutField, UndoSupport undo){
        Set<CyNode> selected = null;
        if(views != null && views.size() > 0)
        {
            selected = new HashSet<CyNode>();
            for (View<CyNode> v : views)
                selected.add(v.getModel());
        }
        this.view = cyView;
        this.undo = undo;
        init(cyView.getModel(), layout, layoutContext, selected, layoutField);
    }

    private void init(CyNetwork net, CyLayoutAlgorithm layout, Object layoutContext, Set<CyNode> selected, String layoutField) {
        this.net = net;
        this.layout = layout;
        this.layoutContext = layoutContext;
        this.layoutField = layoutField;
        this.selectedNodes = selected;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {

//        if (undo != null && view != null)
//        {
//            undo.postEdit(new LayoutEdit("Pair layout [" + (layout == null ? "no layout" : layout.toString()) + "]", view));
//        }

    }
}
