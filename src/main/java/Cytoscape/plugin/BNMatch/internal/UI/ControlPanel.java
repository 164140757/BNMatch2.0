package Cytoscape.plugin.BNMatch.internal.UI;

import Cytoscape.plugin.BNMatch.internal.Tasks.CombineNetworksTask;
import Cytoscape.plugin.BNMatch.internal.Tasks.HGAInputCheckTask;
import Cytoscape.plugin.BNMatch.internal.Tasks.HGATask;
import Cytoscape.plugin.BNMatch.internal.Tasks.PairLayoutTask;
import Cytoscape.plugin.BNMatch.internal.util.InputsAndServices;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
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

import static Cytoscape.plugin.BNMatch.internal.util.InputsAndServices.*;


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
    private JButton analyzeButton;

    private JLabel hVal;
    private JFileChooser simMatrixFileChooser;
    // parameters for HGA
    private JLabel simMatrixLabel;
    private JSlider hValSlider;
    private JTextField tolerance;
    private JTextField seqFactor;
    private JCheckBox forcedCheck;
    private JCheckBox displayOnlyCheckBox;
    private JCheckBox GPUCheckBox;

    private FileNameExtensionFilter excelFileFilter;
    private FileNameExtensionFilter txtFileFilter;
    private JPanel paramsPanel;

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
                simMatrixLabel.setText(selectedFile.getName());
            }
        });
        hValSlider.addChangeListener(actionEvent -> {
            int val = hValSlider.getValue();
            hVal.setText(val + "%");
        });

        // analyseButton settings
        analyzeButton.addActionListener(actionEvent -> {
            setUserInput();
            TaskIterator it = new TaskIterator();
            HGAInputCheckTask checkTask = new HGAInputCheckTask();
            HGATask hgaTask = new HGATask();
            CombineNetworksTask combineNetworksTask = new CombineNetworksTask();
            PairLayoutTask pairLayoutTask = new PairLayoutTask();
            it.append(checkTask);
            it.append(hgaTask);
            it.append(combineNetworksTask);
            it.append(pairLayoutTask);
            taskManager.execute(it);
        });

        // check boxes
        forcedCheck.addActionListener(actionEvent->{
            force = forcedCheck.isSelected();
        });

        displayOnlyCheckBox.addActionListener(actionEvent->{
            onlyDisplay = displayOnlyCheckBox.isSelected();
            if(onlyDisplay){
                simMatrixLabel.setText("Your mapping result from the external file");
                simMatrixFileChooser.setFileFilter(txtFileFilter);
                for (Component component : paramsPanel.getComponents()) {
                    component.setEnabled(false);
                }
                GPUCheckBox.setEnabled(false);
            }
            else{
                simMatrixLabel.setText("Similarity matrix for E-value from BLASTP:");
                simMatrixFileChooser.setFileFilter(excelFileFilter);
                for (Component component : paramsPanel.getComponents()) {
                    component.setEnabled(true);
                }
                GPUCheckBox.setEnabled(true);
            }
        });

        GPUCheckBox.addActionListener(actionEvent->{
            GPU = GPUCheckBox.isSelected();
        });
    }

    private void setUserInput() {
        // shift all parameters from UI to a specific class to export users's information
        // networks
        // check if there's no networks input
        InputsAndServices.indexNetwork = (CyNetwork) indexNetworks.getSelectedItem();
        InputsAndServices.targetNetwork = (CyNetwork) targetNetworks.getSelectedItem();
        InputsAndServices.InputFile = simMatrixFileChooser.getSelectedFile();
        InputsAndServices.hVal = (double) hValSlider.getValue() / 100;
        InputsAndServices.tol = Double.parseDouble(tolerance.getText());
        InputsAndServices.bF = Double.parseDouble(seqFactor.getText());
        InputsAndServices.onlyDisplay = displayOnlyCheckBox.isSelected();
        InputsAndServices.GPU = GPUCheckBox.isSelected();
        InputsAndServices.force = forcedCheck.isSelected();
    }

    public void init() {
        // get all available networks in the app panel
        indexNetworks = new JComboBox<>();
        targetNetworks = new JComboBox<>();
        displayOnlyCheckBox = new JCheckBox("Only display the result");
        GPUCheckBox = new JCheckBox("GPU acceleration");
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
        fileInfoPanel.setBorder(new TitledBorder("Similarity matrix"));

        //parameters panel
        paramsPanel = new JPanel(new MigLayout("wrap 2", "grow", "grow"));
        paramsPanel.setBorder(new TitledBorder("Change the parameters input for HGA"));
        simMatrixLabel = new JLabel("Similarity matrix for E-value from BLASTP:");
        simMatrixBrowseButton = new JButton("Browse");
        simMatrixFileChooser = new JFileChooser();

        // params
        forcedCheck = new JCheckBox("force mapping for same nodes");
        forcedCheck.setSelected(true);
        JLabel hValLabel = new JLabel("Hungarian account:");
        hVal = new JLabel(100 / 2 + "%");
        hValSlider = new JSlider();
        hValSlider.setMaximum(100);
        hValSlider.setValue(100 / 2);
        JLabel toleranceLabel = new JLabel("Tolerance for iteration:");
        tolerance = new JTextField("0.01");
        JLabel seqFactorLabel = new JLabel("weight account for sequence similarity:");
        seqFactor = new JTextField("0.5");

        // analysis button
        analyzeButton = new JButton("Analyze");
        // fileChoosers settings
        setFileChoosers();

        // add to graphsPanel
        graphsPanel.add(new JLabel("Index network:"));
        graphsPanel.add(indexNetworks);
        graphsPanel.add(new JLabel("target network:"));
        graphsPanel.add(targetNetworks);
        graphsPanel.add(displayOnlyCheckBox);
        graphsPanel.add(GPUCheckBox,"wrap");

        fileInfoPanel.add(simMatrixLabel, "wrap");
        fileInfoPanel.add(simMatrixBrowseButton, "wrap");

        // paramsPanel
        paramsPanel.add(forcedCheck, "wrap");
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
        rootPanel.add(analyzeButton, "center");

    }

    private void setFileChoosers() {
        excelFileFilter = new FileNameExtensionFilter("" +
                "Excel file or text file for simMat: local blastp result",
                "xlsx", "xls","txt");
        txtFileFilter = new FileNameExtensionFilter("TEXT FIle for simMat: local blastp result",
                "txt");
        // fileFormat settings
        simMatrixFileChooser.addChoosableFileFilter(excelFileFilter);
        simMatrixFileChooser.addChoosableFileFilter(txtFileFilter);
        // init dictionary
        simMatrixFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }

    private void setMax(JComboBox<?> combo) {
        combo.setMaximumSize(new Dimension(COMBOMAXLENGTH, combo.getMinimumSize().height));
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
