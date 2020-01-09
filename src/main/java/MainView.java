import com.fasterxml.jackson.databind.ObjectMapper;
import model.OpenReqDependency;
import model.OpenReqProject;
import model.OpenReqRequirement;
import model.OpenReqSchema;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainView {

    //The root panel of the GUI
    private JPanel rootPanel;

    private JLabel title;
    private JLabel fileUploadLabel;
    private JLabel projectLabel;
    private JLabel requirementsLabel;
    private JLabel duplicatesLabel;
    private JLabel duplicatesListLabel;


    private JTextField requirementsFile;
    private JComboBox projectComboBox;
    private JButton uploadRequirementsButton;

    private JTable requirementsTable;
    private JScrollPane requirementsScrollPanel;
    private DefaultTableModel requirementsTableModel;

    private JPanel algorithmCheckboxPanel;
    private JCheckBox bm25fCheckbox;
    private JCheckBox fesvmCheckbox;
    private JLabel algorithmLabel;
    private JPanel fesvmPanel;
    private JCheckBox lexicalCheckbox;
    private JCheckBox syntacticCheckbox;

    //Upload duplicates schema
    private JTextField duplicatesSchemaField;
    private JButton uploadDuplicatesButton;

    //Duplicates data (load)
    private JScrollPane duplicatesScrollPane;
    private JTable requirementPairsTable;
    private DefaultTableModel requirementPairsTableModel;
    private JPanel loadDuplicatesPane;

    //Final button set
    private JPanel buttonPanel;
    private JButton optimizeButton;
    private JButton trainButton;
    private JButton testButton;

    private OpenReqSchema openReqSchema;
    private OpenReqSchema duplicateSchema;

    public MainView() {
        initializeGUIComponents();
        setListeners();
        initializeTableModels();
        initializeButtonListeners();
    }

    private void initializeButtonListeners() {
        optimizeButton.addActionListener(e -> {
            //TODO BM25F --> optimize free parameters according to dataset
            //TODO FE-SVM --> train&test with optimization and store best values
        });
        trainButton.addActionListener(e -> {
            //TODO BM25F --> cross-validation and threshold for best value
            //TODO FE-SVM --> train classifier
        });
        testButton.addActionListener(e -> {
            //TODO BM25F --> score with threshold
            //TODO FE-SVM --> predict against classifier
        });
    }

    private void initializeTableModels() {
        requirementsTableModel = new DefaultTableModel(new String[] { "ID", "Name", "Text" }, 0);
        requirementsTable.setModel(requirementsTableModel);
        requirementsTable.getColumnModel().getColumn(0).setMaxWidth(80);
        requirementsTable.getColumnModel().getColumn(1).setMaxWidth(300);
        requirementsTable.setDefaultRenderer(Object.class, new TextTableRenderer());
        requirementsTable.getColumnModel().getColumn(0).setCellRenderer(new TextTableRenderer());
        requirementsTable.getColumnModel().getColumn(1).setCellRenderer(new TextTableRenderer());
        requirementsTable.getColumnModel().getColumn(2).setCellRenderer(new TextTableRenderer());
        
        requirementPairsTableModel = new DefaultTableModel(new String[] {"From ID", "To ID", "Status", "Score"}, 0);
        requirementPairsTable.setModel(requirementPairsTableModel);

    }

    private void initializeGUIComponents() {
        projectComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String project = e.getItem().toString();

                List<String> reqIds = openReqSchema.getProjects().stream().filter(p -> p.getId().equals(project))
                        .map(OpenReqProject::getSpecifiedRequirements).findFirst().orElse(null);

                List<OpenReqRequirement> requirements = openReqSchema.getRequirements().stream()
                        .filter(r -> reqIds.contains(r.getId())).collect(Collectors.toList());

                int rowCount = requirementsTableModel.getRowCount();
                //Remove rows one by one from the end of the table
                for (int i = rowCount - 1; i >= 0; i--) {
                    requirementsTableModel.removeRow(i);
                }
                for (OpenReqRequirement requirement : requirements) {
                    requirementsTableModel.addRow(new String[]{requirement.getId(), requirement.getName(), requirement.getText()});
                }

            }
        });
        //Initialize project as not enabled, before uploading requirements
        projectComboBox.setEnabled(false);
        fesvmPanel.setVisible(false);
    }

    private void setListeners() {
       uploadRequirementsButton.addActionListener(e -> {
           JFileChooser jFileChooser = new JFileChooser();
           jFileChooser.setCurrentDirectory(new File("."));
           jFileChooser.showOpenDialog(null);
           File file =  jFileChooser.getSelectedFile();
           requirementsFile.setText(file.getAbsolutePath());
           projectComboBox.setEnabled(true);
           fillRequirementTables(jFileChooser.getSelectedFile());
       });
       bm25fCheckbox.addItemListener(e -> {
           int state = e.getStateChange();
           if (state == ItemEvent.SELECTED) {
               fesvmCheckbox.setSelected(false);
               fesvmPanel.setVisible(false);
           }
       });
       fesvmCheckbox.addItemListener(e -> {
           int state = e.getStateChange();
           if (state == ItemEvent.SELECTED) {
               bm25fCheckbox.setSelected(false);
               fesvmPanel.setVisible(true);
           }
       });
       uploadDuplicatesButton.addActionListener(e -> {
           JFileChooser jFileChooser = new JFileChooser();
           jFileChooser.setCurrentDirectory(new File("."));
           jFileChooser.showOpenDialog(null);
           File file =  jFileChooser.getSelectedFile();
           duplicatesSchemaField.setText(file.getAbsolutePath());
           fillDuplicatesTable(jFileChooser.getSelectedFile());
       });
    }

    private void fillDuplicatesTable(File selectedFile) {
        for (int i = requirementPairsTableModel.getRowCount() - 1; i >= 0; --i) {
            requirementPairsTableModel.removeRow(i);
        }

        try {
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(selectedFile));

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            String text = sb.toString();
            JSONObject json = new JSONObject(text);

            ObjectMapper objectMapper = new ObjectMapper();
            duplicateSchema = objectMapper.readValue(json.toString(), OpenReqSchema.class);

            int rowCount = requirementPairsTableModel.getRowCount();
            //Remove rows one by one from the end of the table
            for (int i = rowCount - 1; i >= 0; i--) {
                requirementPairsTableModel.removeRow(i);
            }
            for (OpenReqDependency dependency : duplicateSchema.getDependencies()) {
                requirementPairsTableModel.addRow(new String[]{dependency.getFromid(), dependency.getToid(),
                        dependency.getStatus().toString(),
                        dependency.getDependency_score() == null ? "-" : dependency.getDependency_score().toString()});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void fillRequirementTables(File requirementsFile) {
        for (int i = requirementsTableModel.getRowCount() - 1; i >= 0; --i) {
            requirementsTableModel.removeRow(i);
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(requirementsFile));

            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                // sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String text = sb.toString();
            JSONObject json = new JSONObject(text);

            ObjectMapper objectMapper = new ObjectMapper();
            openReqSchema = objectMapper.readValue(json.toString(), OpenReqSchema.class);

            //TODO

            //Fill project combobox
            DefaultComboBoxModel model = new DefaultComboBoxModel(
                    Stream.concat(
                            Arrays.stream(new String[]{"<select project>"}),
                            openReqSchema.getProjects().stream().map(OpenReqProject::getId)
                    ).collect(Collectors.toList()).toArray());
            projectComboBox.setModel(model);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        JFrame jframe = new JFrame("Main view");
        jframe.setContentPane(new MainView().rootPanel);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.pack();
        jframe.setVisible(true);
    }

}
