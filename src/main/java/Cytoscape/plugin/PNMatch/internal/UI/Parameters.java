package Cytoscape.plugin.PNMatch.internal.UI;

import org.cytoscape.model.CyNetwork;

import java.io.File;
import java.util.Vector;

public class Parameters {


    public final boolean force;
    public Vector<Double> params;
    public CyNetwork indexNetwork;
    public CyNetwork targetNetwork;
    public File simMatFile;

    /**
     * @param params hVal, tolerance, bio-factor
     * @param force force mapping for the same nodes
     */
    public Parameters(CyNetwork indexNetwork,
                      CyNetwork targetNetwork,
                      File simMatFile,
                      Vector<Double> params,
                      boolean force) {
        this.indexNetwork = indexNetwork;
        this.targetNetwork = targetNetwork;
        this.simMatFile = simMatFile;
        this.params = params;
        this.force = force;
    }
}
