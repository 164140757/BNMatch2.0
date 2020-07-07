package Cytoscape.plugin.PNMatch.internal.UI;

import Cytoscape.plugin.PNMatch.internal.Tasks.DisplayTask;
import Cytoscape.plugin.PNMatch.internal.Tasks.HGATask;
import UI.TreeTable;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.jgrapht.alg.util.Pair;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Cytoscape.plugin.PNMatch.internal.UI.InputsAndServices.networkManager;


// Define a CytoPanel class
public class ControlPanel implements CytoPanelComponent, NetworkAddedListener, NetworkAboutToBeDestroyedListener {
    private static final int COMBOMAXLENGTH = 100;
    private final TaskManager taskManager;

    // data structures
    // UI components
    private JPanel rootPanel;
    //    private JPanel modeCardPanel;
    private JComboBox<CyNetwork> indexNetworks;
    private JComboBox<CyNetwork> targetNetworks;

    private JButton simMatrixBrowseButton;
    private JButton analyseButton;
    private JButton closeButton;

    private JLabel hVal;
    private JFileChooser simMatrixFileChooser;
    // parameters for HGA
    private int non_zeros_every_row_MAX; // this is the h value to shape the HA matrix by selecting rows having at least h non zero elements
    private boolean isindexQuery;
    private JLabel simMatrixLabel;
    private JSlider hValSlider;
    private JTextField tolerance;
    private JTextField seqFactor;
    private JCheckBox forcedCheck;

    public ControlPanel() {
        this.taskManager = InputsAndServices.taskManager;
        // initialize UI settings
        init();
        // add listeners for buttons
        addListeners();
    }


    private void addListeners() {
        // targetNetworks listener
        targetNetworks.addActionListener(actionEvent -> {
            CyNetwork selectedNetwork = targetNetworks.getItemAt(targetNetworks.getSelectedIndex());
            non_zeros_every_row_MAX = selectedNetwork.getNodeCount();
            // dramatically change the component
            hValSlider.setValue(non_zeros_every_row_MAX / 2);
            hValSlider.setMaximum(non_zeros_every_row_MAX);
            hVal.setText(Integer.toString(non_zeros_every_row_MAX / 2));
            hValSlider.revalidate();
            hVal.revalidate();

        });

        // FileBrowseButtons
        simMatrixBrowseButton.addActionListener(actionEvent -> {
            int status = simMatrixFileChooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File selectedFile = simMatrixFileChooser.getSelectedFile();
                simMatrixLabel.setText(simMatrixLabel.getText() + selectedFile.getName());
            }
        });
        hValSlider.addChangeListener(actionEvent -> {
            int val = hValSlider.getValue();
            hVal.setText(Integer.toString(val));
        });

        // analyseButton settings
        analyseButton.addActionListener(actionEvent -> {
            setUserInput();
            TaskIterator it = new TaskIterator();
            HGATask hgaTask = new HGATask();
            DisplayTask displayTask = new DisplayTask();
            it.append(hgaTask);
            taskManager.execute(it);
        });
        // close
        closeButton.addActionListener(actionEvent -> System.exit(0));

    }



    private void insertToTable(HashMap<String, List<Pair<String, String>>> infoMap, TreeTable treeTable) {

        List<String[]> content = new ArrayList<>();
        for (Map.Entry<String, List<Pair<String, String>>> mapEntry : infoMap.entrySet()) {
            List<Pair<String, String>> rowIndex = mapEntry.getValue();
            String name = mapEntry.getKey();
            // head
            content.add(new String[]{name});
            // ID
            for (Pair<String, String> index : rowIndex) {
                String[] row = new String[3];
                // ID
                row[0] = index.getFirst();
                // name
                row[1] = name;
                // description
                row[2] = index.getSecond();
                content.add(row);
            }
        }
        treeTable.setContent(content);
    }

    private void setUserInput() {
        // shift all parameters from UI to a specific class to export users's information
        // networks
        InputsAndServices.indexNetwork = (CyNetwork) indexNetworks.getSelectedItem();
        InputsAndServices.targetNetwork= (CyNetwork) targetNetworks.getSelectedItem();
        InputsAndServices.simMatFile= simMatrixFileChooser.getSelectedFile();
        InputsAndServices.hVal = hValSlider.getValue();
        InputsAndServices.tol = Double.parseDouble(tolerance.getText());
        InputsAndServices.bF = Double.parseDouble(seqFactor.getText());
    }

    public void init() {
        // get all available networks in the app panel
        indexNetworks = new JComboBox<>();
        targetNetworks = new JComboBox<>();

        // limit the maxSize to display
        setMax(indexNetworks);
        setMax(targetNetworks);

        for (CyNetwork network : networkManager.getNetworkSet()) {
            indexNetworks.addItem(network);
            targetNetworks.addItem(network);
        }
        // check if there're no networks
        if (indexNetworks.getItemCount() != 0) {
            indexNetworks.setSelectedIndex(0);
            targetNetworks.setSelectedIndex(0);
            // initialize parameters
            non_zeros_every_row_MAX = targetNetworks.getItemAt(0).getNodeCount();
        } else {
            non_zeros_every_row_MAX = 0;
        }
        // graphs panel components initialization
        JPanel graphsPanel = new JPanel(new MigLayout("wrap 2", "grow", "grow"));
        graphsPanel.setBorder(new TitledBorder("Networks"));

        // sequences panel components initialization
        JPanel fileInfoPanel = new JPanel(new MigLayout("wrap 2", "grow", "grow"));
        fileInfoPanel.setBorder(new TitledBorder("Protein sequences and similarity matrix"));

        //parameters panel
        JPanel paramsPanel = new JPanel(new MigLayout("wrap 2", "grow", "grow"));
        paramsPanel.setBorder(new TitledBorder("Change the parameters input for HGA"));

        simMatrixLabel = new JLabel("Similarity matrix for E-value from BLASTP:");
        simMatrixBrowseButton = new JButton("Browse");
        simMatrixFileChooser = new JFileChooser();

        // params
        forcedCheck = new JCheckBox("Map same nodes in a prefuse.util.force way");
        forcedCheck.setSelected(true);
        JLabel hValLabel = new JLabel("Non-zero items per row for H-matrix:");
        hVal = new JLabel(Integer.toString(non_zeros_every_row_MAX / 2));
        hValSlider = new JSlider();
        hValSlider.setMaximum(non_zeros_every_row_MAX);
        hValSlider.setValue(non_zeros_every_row_MAX / 2);
        JLabel toleranceLabel = new JLabel("Tolerance for iteration:");
        tolerance = new JTextField("0.01");
        JLabel seqFactorLabel = new JLabel("weight account for sequence similarity:");
        seqFactor = new JTextField("0.5");

        // analysis button
        analyseButton = new JButton("Analyse");
        closeButton = new JButton("Close");
        // fileChoosers settings
        setFileChoosers();

        // add to graphsPanel
        graphsPanel.add(new JLabel("Index network:"));
        graphsPanel.add(indexNetworks);
        graphsPanel.add(new JLabel("target network:"));
        graphsPanel.add(targetNetworks);

        fileInfoPanel.add(simMatrixLabel, "wrap");
        fileInfoPanel.add(simMatrixBrowseButton, "wrap");

        // paramsPanel
        paramsPanel.add(forcedCheck,"wrap");
        paramsPanel.add(hValLabel);
        paramsPanel.add(hVal);
        paramsPanel.add(hValSlider, "wrap");
        paramsPanel.add(toleranceLabel);
        paramsPanel.add(tolerance);
        paramsPanel.add(seqFactorLabel);
        paramsPanel.add(seqFactor);
        // rootPanel
        rootPanel = new JPanel(new MigLayout("wrap 1", "[grow]", "[grow]"));
        rootPanel.add(graphsPanel, "grow");
        rootPanel.add(fileInfoPanel, "grow");
        rootPanel.add(paramsPanel, "grow");
        rootPanel.add(analyseButton, "center");


    }

    private void setFileChoosers() {
        FileNameExtensionFilter faaFileFilter = new FileNameExtensionFilter("" +
                "FASTA format: nucleotide sequences or amino acid (protein) sequences",
                "fasta", "fna", "ffn", "faa", "frn");
        FileNameExtensionFilter txtFileFilter = new FileNameExtensionFilter("TEXT FIle format",
                "txt");
        // fileFormat settings

        simMatrixFileChooser.addChoosableFileFilter(faaFileFilter);
        simMatrixFileChooser.addChoosableFileFilter(txtFileFilter);
        simMatrixFileChooser.setFileFilter(faaFileFilter);


        // init dictionary
        simMatrixFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }

    private void setMax(JComboBox<?> combo) {
        combo.setMaximumSize(new Dimension(COMBOMAXLENGTH, combo.getMinimumSize().height));
    }

    private void initIDColumn(JComboBox<CyNetwork> networks, JComboBox<CyColumn> idColumns) {
        if (networks.getItemCount() != 0) {
            CyTable table = networks.getItemAt(0).getDefaultNetworkTable();
            for (CyColumn column : table.getColumns()) {
                String name = column.getNameOnly();
                if (name.equals("SUID") || name.equals("Annotations") || name.equals("selected")) {
                    continue;
                }
                idColumns.addItem(column);
            }


        }
    }


    //**********CYTOSCAPE CytoPanelComponent Interface**************
    @Override
    public Component getComponent() {
        return rootPanel;
    }

    @Override
    public CytoPanelName getCytoPanelName() {
        return CytoPanelName.WEST;
    }

    @Override
    public String getTitle() {
        return "BNMatch";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    //**********CYTOSCAPE networks status listener interface**************
    @Override
    public void handleEvent(NetworkAboutToBeDestroyedEvent networkAboutToBeDestroyedEvent) {
        indexNetworks.removeItem(networkAboutToBeDestroyedEvent.getNetwork());
        targetNetworks.removeItem(networkAboutToBeDestroyedEvent.getNetwork());
    }

    @Override
    public void handleEvent(NetworkAddedEvent networkAddedEvent) {
        indexNetworks.addItem(networkAddedEvent.getNetwork());
        targetNetworks.addItem(networkAddedEvent.getNetwork());
    }


}
