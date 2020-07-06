package Cytoscape.plugin.PNMatch.internal.UI;

import Cytoscape.plugin.PNMatch.internal.Tasks.DisplayTask;
import Cytoscape.plugin.PNMatch.internal.Tasks.HGATask;
import UI.TreeTable;
import net.miginfocom.swing.MigLayout;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.*;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.jgrapht.alg.util.Pair;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;


// Define a CytoPanel class
public class ControlPanel implements CytoPanelComponent, NetworkAddedListener, NetworkAboutToBeDestroyedListener {
    private static final int COMBOMAXLENGTH = 100;

    private final CyNetworkManager networkManager;
    private final CyNetworkFactory networkFactory;
    private final CyNetworkNaming namingUtil;
    private final TaskManager taskManager;
    private final CyServiceRegistrar csr;
    private final UndoSupport uds;
    private final CyNetworkViewManager nvm;

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
    //    private JRadioButton sequencesLocalButton;
//    private JRadioButton sequencesRemoteButton;
//    private CardLayout cardsForModes;
    // parameters for HGA
    private int non_zeros_every_row_MAX; // this is the h value to shape the HA matrix by selecting rows having at least h non zero elements
    private boolean isindexQuery;
    private JLabel simMatrixLabel;
    private JSlider hValSlider;
    private JTextField tolerance;
    private JTextField seqFactor;
    private JCheckBox forcedCheck;


    public ControlPanel(
            CyNetworkManager networkManager,
            CyNetworkFactory networkFactory,
            TaskManager taskManager,
            CyNetworkNaming namingUtil,
            CyServiceRegistrar csr,
            UndoSupport uds,
            CyNetworkViewManager nvm
            ) {
        // params
        this.networkManager = networkManager;
        this.networkFactory = networkFactory;
        this.taskManager = taskManager;
        this.namingUtil = namingUtil;
        this.csr = csr;
        this.uds = uds;
        this.nvm = nvm;
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

            // update the list
            // clean the list
//            remoteTargetTableIDColumns.removeAllItems();
//            CyTable remoteTargetTable = selectedNetwork.getDefaultNodeTable();
//            for (CyColumn column : remoteTargetTable.getColumns()) {
//                String name = column.getNameOnly();
//                if (name.equals("SUID") || name.equals("Annotations") || name.equals("selected")) {
//                    continue;
//                }
//                remoteTargetTableIDColumns.addItem(column);
//            }
//            // dramatically change the component
//            remoteTargetTableIDColumns.revalidate();
        });
//
//        // indexNetworks listener
//        indexNetworks.addActionListener(actionEvent -> {
//            // clean the list first
//            remoteIndexTableIDColumns.removeAllItems();
//            CyNetwork selectedNetwork = indexNetworks.getItemAt(indexNetworks.getSelectedIndex());
//            CyTable remoteIndexTable = selectedNetwork.getDefaultNodeTable();
//            for (CyColumn column : remoteIndexTable.getColumns()) {
//                String name = column.getNameOnly();
//                if (name.equals("SUID") || name.equals("Annotations") || name.equals("selected")) {
//                    continue;
//                }
//                remoteIndexTableIDColumns.addItem(column);
//            }
//            remoteIndexTableIDColumns.revalidate();
//        });

        // radioCheckBoxes
//        sequencesRemoteButton.addActionListener(actionEvent -> {
//            cardsForModes.show(modeCardPanel, REMOTEMODE);
//        });
//        sequencesLocalButton.addActionListener(actionEvent -> {
//            cardsForModes.show(modeCardPanel, LOCALMODE);
//        });
//        indexQueryButton.addActionListener(actionEvent -> {
//            // use to contract the code
//            isindexQuery = true;
//            startQueryFrame();
//            isindexQuery = false;
//        });
//        targetQueryButton.addActionListener(actionEvent -> {
//            startQueryFrame();
//        });
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
            Parameters params = getParameters();
            TaskIterator it = new TaskIterator();
            HGATask hgaTask = new HGATask(networkFactory,params);
            DisplayTask displayTask = new DisplayTask(csr,uds,namingUtil,networkFactory,nvm,hgaTask);
            it.append(hgaTask);
            taskManager.execute(it);
        });
        // close
        closeButton.addActionListener(actionEvent -> System.exit(0));

    }

    // open up a new dialog for protein sequences query through web services
    private void startQueryFrame() {
        JFrame qFrame = new JFrame();
        // default window close operation
        qFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // frame attributes settings
        qFrame.setLayout(new BorderLayout());
        qFrame.setTitle("Cytoscape: BNMatch");

        // set a new contentPane
        JPanel contentPane = new JPanel(new MigLayout("wrap 1", "grow", "grow"));
        addComponents(contentPane);
        // set content panel
        qFrame.add(contentPane, BorderLayout.CENTER);
        qFrame.pack();
        // put the frame at the center of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        qFrame.setLocation(dim.width / 2 - qFrame.getSize().width / 2, dim.height / 2 - qFrame.getSize().height / 2);
        qFrame.setVisible(true);
    }


    private void addComponents(JPanel contentPane) {
//        // UI
//        JPanel virusPanel = new JPanel(new MigLayout("wrap 1", "grow", "grow"));
//        JPanel organismPanel = new JPanel(new MigLayout("wrap 1", "grow", "grow"));
//        JPanel tablePanel = new JPanel(new MigLayout("wrap 1", "grow", "grow"));
//        JLabel virusLabel = new JLabel("Is it virus?");
//        // different choices for virus
//        ButtonGroup bg = new ButtonGroup();
//        JRadioButton yesButton = new JRadioButton("yes");
//        JRadioButton noButton = new JRadioButton("no");
//        bg.add(yesButton);
//        bg.add(noButton);
//        // button panel for layout
//        JPanel buttonPanel = new JPanel(new MigLayout("wrap 2"));
//        buttonPanel.add(yesButton);
//        buttonPanel.add(noButton);
//        JLabel treeLabel = new JLabel("Please check proteins:");
//        // add items
//        virusPanel.add(virusLabel);
//        virusPanel.add(buttonPanel);
//        virusPanel.add(treeLabel);

//        //organism panel
//        JLabel orgLabel = new JLabel("Please select the organism to search:");
//        JComboBox<String> orgBox = new JComboBox<>();
//        for (OrganismName organismName : OrganismName.getAll()) {
//            orgBox.addItem(organismName.name);
//        }
//        organismPanel.add(orgLabel);
//        organismPanel.add(orgBox);
//        // add Tree
//        TreeTable treeTable = new TreeTable(Arrays.asList("UniprotID", "Name", "Description"));
//        // add content
//        CyNetwork indexNetwork = indexNetworks.getItemAt(indexNetworks.getSelectedIndex());
//        CyNetwork targetNetwork = targetNetworks.getItemAt(targetNetworks.getSelectedIndex());
//        if (indexNetwork != null && targetNetwork != null) {
//            if (isindexQuery) {
//                AdjList indexAdj = convert(indexNetwork);
//                WebServices.organismName = orgBox.getItemAt(orgBox.getSelectedIndex());
//                WebServices.Uniprot uIndex = new WebServices.Uniprot(indexAdj.getAllNodes());
//                insertToTable(WebServices.infoMap, treeTable);
//            } else {
//                AdjList targetAdj = convert(targetNetwork);
//                // set up before
//                WebServices.organismName = orgBox.getItemAt(orgBox.getSelectedIndex());
//                WebServices.Uniprot uTarget = new WebServices.Uniprot(targetAdj.getAllNodes());
//                insertToTable(WebServices.infoMap, treeTable);
//            }
//        }
//        JScrollPane scrollPane = new JScrollPane(treeTable.getTreeTable());
//        tablePanel.add(scrollPane);

        // info loading
        // load components
//        contentPane.add(virusPanel, "grow");
//        contentPane.add(organismPanel);
//        contentPane.add(tablePanel, "grow");
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

    private Parameters getParameters() {
        // shift all parameters from UI to a specific class to export users's information
        // networks
        CyNetwork indexNetwork = (CyNetwork) indexNetworks.getSelectedItem();
        CyNetwork targetNetwork = (CyNetwork) targetNetworks.getSelectedItem();
        // sequences
        CyColumn indexIDColumn;
        CyColumn targetIDColumn;
        File indexSeqFile;
        File targetSeqFile;
        File simMatrixFile;
//        // remote mode
//        if (sequencesRemoteButton.isSelected()) {
//            // ID columns
//            indexIDColumn = (CyColumn) remoteIndexTableIDColumns.getSelectedItem();
//            targetIDColumn = (CyColumn) remoteTargetTableIDColumns.getSelectedItem();
//            return new Parameters(indexNetwork, targetNetwork, indexIDColumn, targetIDColumn);
//        }
        // local mode

        // sequence files
        simMatrixFile = simMatrixFileChooser.getSelectedFile();
        double hVal = hValSlider.getValue();
        double tol = Double.parseDouble(tolerance.getText());
        double bfac = Double.parseDouble(seqFactor.getText());
        Vector<Double> vector = new Vector<>(Arrays.asList(hVal, tol, bfac));
        return new Parameters(indexNetwork, targetNetwork, simMatrixFile, vector,forcedCheck.isSelected());

    }

    public void init() {
        // get all available networks in the app panel
        indexNetworks = new JComboBox<>();
        targetNetworks = new JComboBox<>();


        // limit the maxSize to display
        setMax(indexNetworks);
        setMax(targetNetworks);

        for (CyNetwork network :
                networkManager.getNetworkSet()) {
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
//        sequencesLocalButton = new JRadioButton("Local");
//        sequencesRemoteButton = new JRadioButton("Remote", true);
//        indexQueryButton = new JButton("Query");
        // model select panel cardLayout
//        cardsForModes = new CardLayout();
//        modeCardPanel = new JPanel(cardsForModes);
//        JPanel remoteMode = new JPanel(new MigLayout("wrap 2"));
//        JPanel localMode = new JPanel(new MigLayout("wrap 2"));


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


//        indexQueryButton = new JButton("index query");
//        targetQueryButton = new JButton("target query");
        // analysis button
        analyseButton = new JButton("Analyse");
        closeButton = new JButton("Close");
        // fileChoosers settings
        setFileChoosers();
        // different visualize formats when different buttons have been clicked
//        ButtonGroup sequencesCheckBg = new ButtonGroup();
//        sequencesCheckBg.add(sequencesLocalButton);
//        sequencesCheckBg.add(sequencesRemoteButton);
        // button panel for layout
//        JPanel buttonPanel = new JPanel(new MigLayout("wrap 2"));
//        buttonPanel.add(sequencesLocalButton);
//        buttonPanel.add(sequencesRemoteButton);

        // add to graphsPanel
        graphsPanel.add(new JLabel("Index network:"));
        graphsPanel.add(indexNetworks);
        graphsPanel.add(new JLabel("target network:"));
        graphsPanel.add(targetNetworks);


        // add to sequencePanel
//        sequencesPanel.add(new JLabel("Load methods:"), "wrap");
//        sequencesPanel.add(buttonPanel, "wrap");
//        remoteMode.add(indexQueryButton);
//        remoteMode.add(targetQueryButton);
//        localMode.add(indexLocalLabel, "wrap");
//        localMode.add(indexLocalBrowseButton, "wrap");
//        localMode.add(targetLocalLabel, "wrap");
//        localMode.add(targetLocalBrowseButton, "wrap");
        fileInfoPanel.add(simMatrixLabel, "wrap");
        fileInfoPanel.add(simMatrixBrowseButton, "wrap");
//        sequencesPanel.add(modeCardPanel, "wrap,grow");

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
