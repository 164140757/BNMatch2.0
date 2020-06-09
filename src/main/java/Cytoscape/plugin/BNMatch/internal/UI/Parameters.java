package Cytoscape.plugin.BNMatch.internal.UI;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import java.io.File;
import java.util.Vector;

public class Parameters {


    private Vector<Double> params;
    public CyNetwork indexNetwork;
    public CyNetwork targetNetwork;
    public File indexSeqFile;
    public File targetSeqFile;
    public File simMatFile;

    public Parameters(CyNetwork indexNetwork,
                      CyNetwork targetNetwork,
                      File indexSeqFile,
                      File targetSeqFile,
                      File simMatFile,
                      Vector<Double> params) {
        this.indexNetwork = indexNetwork;
        this.targetNetwork = targetNetwork;
        this.indexSeqFile = indexSeqFile;
        this.targetSeqFile = targetSeqFile;
        this.simMatFile = simMatFile;
        this.params = params;
    }
}
