package Cytoscape.plugin.PNMatcher.internal.UI;

import Cytoscape.plugin.PNMatcher.internal.Tasks.DisplayTask;
import Cytoscape.plugin.PNMatcher.internal.Tasks.HGATask;
import Cytoscape.plugin.PNMatcher.internal.Tasks.PairLayoutTask;
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

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import static Cytoscape.plugin.PNMatcher.internal.UI.InputsAndServices.networkManager;


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

    private JLabel hVal;
    private JFileChooser simMatrixFileChooser;
    // parameters for HGA
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
            hVal.setText(val +"%");
        });

        // analyseButton settings
        analyseButton.addActionListener(actionEvent -> {
            setUserInput();
            TaskIterator it = new TaskIterator();
            HGATask hgaTask = new HGATask();
//            it.append(hgaTask);
//            taskManager.execute(it);
//            it.append(displayTask);
            // it append(pairLayoutTask);
            DisplayTask displayTask = new DisplayTask();
            PairLayoutTask pairLayoutTask = new PairLayoutTask();
        });

    }

    private void setUserInput() {
        // shift all parameters from UI to a specific class to export users's information
        // networks
        // check if there's no networks input

        InputsAndServices.indexNetwork = (CyNetwork) indexNetworks.getSelectedItem();
        InputsAndServices.targetNetwork= (CyNetwork) targetNetworks.getSelectedItem();
        if(InputsAndServices.indexNetwork == null || InputsAndServices.targetNetwork == null){
            System.out.println("Both index-network and target-network should be selected.");
        }
        InputsAndServices.simMatFile= simMatrixFileChooser.getSelectedFile();
        if(InputsAndServices.simMatFile == null){
            System.out.println("Similarity matrix has been loaded.");
        }
        InputsAndServices.hVal = (double)hValSlider.getValue()/100;
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
        forcedCheck = new JCheckBox("except for same nodes");
        forcedCheck.setSelected(true);
        JLabel hValLabel = new JLabel("Hungarian account:");
        hVal = new JLabel(100 / 2 +"%");
        hValSlider = new JSlider();
        hValSlider.setMaximum(100);
        hValSlider.setValue(100 / 2);
        JLabel toleranceLabel = new JLabel("Tolerance for iteration:");
        tolerance = new JTextField("0.01");
        JLabel seqFactorLabel = new JLabel("weight account for sequence similarity:");
        seqFactor = new JTextField("0.5");

        // analysis button
        analyseButton = new JButton("Analyse");
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
                "CSV format: local blastp result",
                "csv");
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
        return "PNMatcher";
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
