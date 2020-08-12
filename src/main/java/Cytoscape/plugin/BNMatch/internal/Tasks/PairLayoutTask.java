package Cytoscape.plugin.BNMatch.internal.Tasks;

import Cytoscape.plugin.BNMatch.internal.UI.InputsAndServices;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static Cytoscape.plugin.BNMatch.internal.Canvas.ShapeColor.randomColor;
import static Cytoscape.plugin.BNMatch.internal.Canvas.ShapeColor.randomShapeAndColor;
import static Cytoscape.plugin.BNMatch.internal.util.CytoUtils.getCyNetworkView;

public class PairLayoutTask extends AbstractTask {

    private static final double HORIZONTAL_SCALE = 1.3;
    private static final Double WIDTH_SCALE = 3.;
    private VisualMappingFunctionFactory continuousMappingFactoryServiceRef;
    private CyNetworkFactory networkFactory;
    private CyNetworkViewManager viewManager;
    private CyNetworkManager networkManager;
    private CyNetworkViewFactory viewFactory;
    private CyEventHelper eventHelper;
    private VisualStyleFactory visualStyleFactoryServiceRef;
    private Map<CyNode, double[]> indexPos;

    public PairLayoutTask() {

    }


    @Override
    public void run(TaskMonitor taskMonitor) {
        taskMonitor.setStatusMessage("adjusting nodes' positions...");
        networkManager = InputsAndServices.networkManager;
        viewFactory = InputsAndServices.networkViewFactory;
        eventHelper = InputsAndServices.eventHelper;
        continuousMappingFactoryServiceRef = InputsAndServices.mapFactoryService;
        networkFactory = InputsAndServices.networkFactory;
        viewManager = InputsAndServices.networkViewManager;
        //pair layout setting
        CyNetwork combinedNet = AlignmentTaskData.combinedNet;
        networkManager.addNetwork(combinedNet);
        CyNetworkView view = getCyNetworkView(combinedNet, viewManager, viewFactory);
        oldViewToNew(view);
        createPair(view);

    }

    private void oldViewToNew(CyNetworkView view) {
        CyNetwork indexNetwork = InputsAndServices.indexNetwork;
        CyNetwork tgtNetwork = InputsAndServices.targetNetwork;
        CyNetworkView indView = getCyNetworkView(indexNetwork, viewManager, viewFactory);
        CyNetworkView tgtView = getCyNetworkView(tgtNetwork, viewManager, viewFactory);
        double shift = getShift(indView, tgtView);
        copyViewLocationTo(indView, AlignmentTaskData.indexOldToNew, view, 0.);
        shiftViewLocationTo(AlignmentTaskData.cyNodeMapping, view, shift);
    }

    private void shiftViewLocationTo(HashMap<CyNode, CyNode> mapping, CyNetworkView to, double xff) {
        mapping.forEach((n1, n2) -> {
            double x = to.getNodeView(n1).getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            double y = to.getNodeView(n1).getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
            if(n2!=null){
                to.getNodeView(n2).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x + xff);
                to.getNodeView(n2).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
            }
        });

    }

    private double getShift(CyNetworkView indView, CyNetworkView tgtView) {
        double idV = hScale(indView);
        return idV * HORIZONTAL_SCALE;
    }

    private double hScale(CyNetworkView indView) {
        Map<CyNode, double[]> positions = getPositions(indView);
        double minx = Double.POSITIVE_INFINITY;
        double maxx = Double.NEGATIVE_INFINITY;
        for (double[] p : positions.values()) {
            minx = Math.min(minx, p[0]);
            maxx = Math.max(maxx, p[0]);
        }
        return maxx - minx;
    }

    private void copyViewLocationTo(CyNetworkView from, HashMap<CyNode, CyNode> oldToNew, CyNetworkView to, double xff) {
        from.getNodeViews().forEach(
                f -> {
                    CyNode node = f.getModel();
                    CyNode tNode = oldToNew.get(node);
                    double x = f.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
                    double y = f.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
                    to.getNodeView(tNode).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y);
                    to.getNodeView(tNode).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x + xff);
                }
        );
    }


    private void createPair(CyNetworkView view) {
        HashMap<CyNode, CyNode> mapping = AlignmentTaskData.cyNodeMapping;
        mapping.forEach((index, target) -> {
            View<CyNode> indexV = view.getNodeView(index);
            if(target!=null){
                View<CyNode> tgtV = view.getNodeView(target);
                indexV.setVisualProperty(BasicVisualLexicon.NODE_LABEL,
                        view.getModel().getRow(indexV.getModel()).get(CyNetwork.NAME, String.class));
                tgtV.setVisualProperty(BasicVisualLexicon.NODE_LABEL,
                        view.getModel().getRow(tgtV.getModel()).get(CyNetwork.NAME, String.class));
                // color and shape
                Pair<Color, NodeShape> toAdd = randomShapeAndColor();
                indexV.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, toAdd.getSecond());
                tgtV.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, toAdd.getSecond());
                indexV.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, toAdd.getFirst());
                tgtV.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, toAdd.getFirst());
            }
            // index miss match
            else{
                indexV.setVisualProperty(BasicVisualLexicon.NODE_LABEL,
                        view.getModel().getRow(indexV.getModel()).get(CyNetwork.NAME, String.class));
                indexV.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, Color.white);
            }
        });
        // turn mapping edges to bold style
        HashMap<DefaultWeightedEdge, CyEdge> map1 = AlignmentTaskData.edgeCyEdgeMap1;
        HashMap<DefaultWeightedEdge, CyEdge> map2 = AlignmentTaskData.edgeCyEdgeMap2;
        AlignmentTaskData.mappingEdges.forEach(edgeEdgePair -> {
            DefaultWeightedEdge edge1 = edgeEdgePair.getFirst();
            DefaultWeightedEdge edge2 = edgeEdgePair.getSecond();
            CyEdge cyEdge1 = map1.get(edge1);
            CyEdge cyEdge2 = map2.get(edge2);
            view.getEdgeView(cyEdge1).setVisualProperty(BasicVisualLexicon.EDGE_WIDTH,
                    view.getEdgeView(cyEdge1).getVisualProperty(BasicVisualLexicon.EDGE_WIDTH)*WIDTH_SCALE);
            view.getEdgeView(cyEdge2).setVisualProperty(BasicVisualLexicon.EDGE_WIDTH,
                    view.getEdgeView(cyEdge2).getVisualProperty(BasicVisualLexicon.EDGE_WIDTH)*WIDTH_SCALE);
            view.getEdgeView(cyEdge1).setVisualProperty(BasicVisualLexicon.EDGE_PAINT,Color.RED);
            view.getEdgeView(cyEdge2).setVisualProperty(BasicVisualLexicon.EDGE_PAINT,Color.RED);
        });

        view.fitContent();
        view.updateView();
        view.fitSelected();
        viewManager.addNetworkView(view, true);
        eventHelper.flushPayloadEvents();
    }


    private Map<CyNode, double[]> getPositions(CyNetworkView view) {
        Map<CyNode, double[]> positions = new HashMap<>();
        for (View<CyNode> v : view.getNodeViews()) {
            double x = v.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
            double y = v.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
            positions.put(v.getModel(), new double[]{x, y});
        }
        return positions;
    }
}
