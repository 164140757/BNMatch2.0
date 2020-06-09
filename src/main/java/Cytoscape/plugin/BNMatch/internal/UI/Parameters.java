package Cytoscape.plugin.BNMatch.internal.UI;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import java.io.File;

public class Parameters {


    public CyNetwork indexNetwork;
    public CyNetwork targetNetwork;
    public CyColumn indexIDColumn;
    public CyColumn targetIDColumn;
    public File indexSeqFile;
    public File targetSeqFile;

    public Parameters(CyNetwork indexNetwork,
                      CyNetwork targetNetwork,
                      CyColumn indexIDColumn,
                      CyColumn targetIDColumn) {
        this.indexNetwork = indexNetwork;
        this.targetNetwork = targetNetwork;
        this.indexIDColumn = indexIDColumn;
        this.targetIDColumn = targetIDColumn;
    }

    public Parameters(CyNetwork indexNetwork,
                      CyNetwork targetNetwork,
                      File indexSeqFile,
                      File targetSeqFile) {
        this.indexNetwork = indexNetwork;
        this.targetNetwork = targetNetwork;
        this.indexSeqFile = indexSeqFile;
        this.targetSeqFile = targetSeqFile;
    }
}
