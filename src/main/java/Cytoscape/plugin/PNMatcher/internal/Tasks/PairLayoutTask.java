package Cytoscape.plugin.PNMatcher.internal.Tasks;

import Cytoscape.plugin.PNMatcher.internal.UI.InputsAndServices;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.jgrapht.alg.util.Pair;

import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static Cytoscape.plugin.PNMatcher.internal.Canvas.ShapeColor.randomShapeAndColor;
import static Cytoscape.plugin.PNMatcher.internal.util.CytoUtils.getCyNetworkView;

public class PairLayoutTask extends AbstractTask {

    private static final double HORIZONTAL_SCALE = 1.2;
    private VisualMappingFunctionFactory continuousMappingFactoryServiceRef;
    private CyNetworkFactory networkFactory;
    private CyNetworkViewManager viewManager;
    private CyNetworkManager networkManager;
    private CyNetworkViewFactory viewFactory;
    private CyEventHelper eventHelper;
    private VisualStyleFactory visualStyleFactoryServiceRef;

    public PairLayoutTask() {
        networkManager = InputsAndServices.networkManager;
        viewFactory = InputsAndServices.networkViewFactory;
        eventHelper = InputsAndServices.eventHelper;
        continuousMappingFactoryServiceRef = InputsAndServices.mapFactoryService;
        networkFactory = InputsAndServices.networkFactory;
        viewManager = InputsAndServices.networkViewManager;
        //pair layout setting
        CyNetwork combinedNet = AlignmentTaskData.combinedNet;
        networkManager.addNetwork(combinedNet);
        CyNetworkView view = getCyNetworkView(combinedNet,viewManager,viewFactory);
        oldViewToNew(view);
        createPair(view);
    }



    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setStatusMessage("adjusting nodes' positions...");
        networkManager = InputsAndServices.networkManager;
        viewFactory = InputsAndServices.networkViewFactory;
        eventHelper = InputsAndServices.eventHelper;
        //pair layout setting
        CyNetwork combinedNet = AlignmentTaskData.combinedNet;
        networkManager.addNetwork(combinedNet);
        CyNetworkView view = viewFactory.createNetworkView(combinedNet);
        oldViewToNew(view);
        createPair(view);

    }

    private void oldViewToNew(CyNetworkView view) {
        CyNetwork indexNetwork = InputsAndServices.indexNetwork;
        CyNetwork tgtNetwork = InputsAndServices.targetNetwork;
        CyNetworkView indView = getCyNetworkView(indexNetwork,viewManager,viewFactory);
        CyNetworkView tgtView = getCyNetworkView(tgtNetwork,viewManager,viewFactory);
        double shift = getShift(indView,tgtView);
        copyViewLocationTo(indView,AlignmentTaskData.indexOldToNew,view,0.);
        copyViewLocationTo(tgtView,AlignmentTaskData.targetOldToNew,view,shift);
    }

    private double getShift(CyNetworkView indView, CyNetworkView tgtView) {
        double idV = hScale(indView);
        double tgtV = hScale(tgtView);
        return (tgtV-idV)*HORIZONTAL_SCALE;
    }

    private double hScale(CyNetworkView indView) {
        Map<CyNode, double[]> positions = getPositions(indView);
        double minx = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        for(double[] p : positions.values())
        {
            minx = Math.min(minx, p[0]);
            maxx = Math.max(maxx, p[0]);
        }
        return maxx-minx;
    }

    private void copyViewLocationTo(CyNetworkView from, HashMap<CyNode, CyNode> oldToNew, CyNetworkView to,double shift) {
        from.getNodeViews().forEach(
                f->{
                    CyNode node = f.getModel();
                    CyNode tNode = oldToNew.get(node);
                    double x = f.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
                    double y = f.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                    to.getNodeView(tNode).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
                    to.getNodeView(tNode).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, x+shift);
                }
        );
    }


    private void createPair(CyNetworkView view) {
        HashMap<CyNode, CyNode> mapping = AlignmentTaskData.cyNodeMapping;
        mapping.forEach((index, target) -> {
            if(index!=null&&target!=null) {
                View<CyNode> indexV = view.getNodeView(index);
                View<CyNode> tgtV = view.getNodeView(target);
                // location set parallel
                double y = indexV.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                tgtV.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
                // color and shape
                Pair<Color, NodeShape> toAdd = randomShapeAndColor();
                indexV.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, toAdd.getSecond());
                tgtV.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, toAdd.getSecond());
                indexV.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, toAdd.getFirst());
                tgtV.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, toAdd.getFirst());
            }
        });
        view.fitContent();
        view.updateView();
        viewManager.addNetworkView(view,true);
        eventHelper.flushPayloadEvents();
    }

    private Map<CyNode, double[]> getPositions(CyNetworkView view) {
        Map<CyNode, double[]> positions = new HashMap<>();
        for(View<CyNode> v : view.getNodeViews())
        {
            double x = v.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            double y = v.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
            positions.put(v.getModel(), new double[] {x, y});
        }
        return positions;
    }
}
