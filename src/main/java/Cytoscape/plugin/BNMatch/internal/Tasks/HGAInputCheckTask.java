package Cytoscape.plugin.BNMatch.internal.Tasks;

import Cytoscape.plugin.BNMatch.internal.UI.InputsAndServices;
import Internal.Algorithms.Graph.HGA.HGA;
import Internal.Algorithms.Graph.Utils.AdjList.UndirectedGraph;
import Internal.Algorithms.Graph.Utils.SimMat;
import Internal.Algorithms.IO.GraphFileReader;
import Internal.Algorithms.IO.MappingResultReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.io.File;

import static Cytoscape.plugin.BNMatch.internal.Tasks.HGATask.convert;

public class HGAInputCheckTask extends AbstractTask {
    public HGAInputCheckTask() {

    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        // shift all parameters from UI to a specific class to export users's information
        // networks
        // check if there's no networks input
        if (InputsAndServices.indexNetwork == null || InputsAndServices.targetNetwork == null) {
            taskMonitor.setTitle("Load Error! Please select your graphs.");
            taskMonitor.showMessage(TaskMonitor.Level.ERROR,"Input error.");
            InputsAndServices.logger.error("Both index-network and target-network should be selected.");
            return;
        }
        if (InputsAndServices.InputFile == null) {
            taskMonitor.setTitle("Load Error! Please input your similarity matrix.");
            taskMonitor.showMessage(TaskMonitor.Level.ERROR,"Input error.");
            InputsAndServices.logger.error("Similarity matrix has been loaded.");
            return;
        }
        CyNetwork indexNetwork = InputsAndServices.indexNetwork;
        CyNetwork targetNetwork = InputsAndServices.targetNetwork;
        File inputFile = InputsAndServices.InputFile;
        UndirectedGraph indNet = convert(indexNetwork);
        UndirectedGraph tgtNet = convert(targetNetwork);
        String[] strArray = InputsAndServices.InputFile.getName().split("\\.");
        String format = strArray[strArray.length - 1];
        SimMat simMat;
        if(!InputsAndServices.onlyDisplay){
            GraphFileReader reader = new GraphFileReader(true, false, false);
            if (format.equals("txt")) {
                simMat = reader.readToSimMat(inputFile, indNet.getAllNodes(), tgtNet.getAllNodes(), true);
            } else if (format.equals("xlsx") || format.equals("xls")) {
                simMat = reader.readToSimMatExcel(inputFile, indNet.getAllNodes(), tgtNet.getAllNodes());
            } else {
                taskMonitor.setTitle("Load Error! Please check your excel file format which should have be xls or xlsx");
                taskMonitor.showMessage(TaskMonitor.Level.ERROR,"Input error.");
                InputsAndServices.logger.error("Your excel file is not xls or xlsx.");
                return;
            }
            // check
            if (!simMat.getRowSet().containsAll(indNet.getAllNodes()) || !simMat.getColSet().containsAll(tgtNet.getAllNodes())) {
                taskMonitor.setTitle("Load Error! The similarity file doesn't contain all nodes in the graphs you have selected.");
                taskMonitor.showMessage(TaskMonitor.Level.ERROR,"Input error.");
                InputsAndServices.logger.error("The similarity file doesn't contain all nodes in the graphs you have selected.");
                return;
            }
            InputsAndServices.siMat = simMat;
        }
        else{

            MappingResultReader reader = new MappingResultReader(inputFile.getAbsolutePath());
            InputsAndServices.mapping = reader.getMapping();
            // check
            if (!indNet.getAllNodes().containsAll(InputsAndServices.mapping.keySet()) ||
                    !tgtNet.getAllNodes().containsAll(InputsAndServices.mapping.values())) {
                taskMonitor.setTitle("Load Error! Your mapping file input havs conflicts with your graphs selected. Please check again.");
                taskMonitor.showMessage(TaskMonitor.Level.ERROR,"Input error.");
                InputsAndServices.logger.error("Your mapping file input havs conflicts with your graphs selected. Please check again.");
                return;
            }
        }
        InputsAndServices.indNet = indNet;
        InputsAndServices.tgtNet = tgtNet;
    }
}
