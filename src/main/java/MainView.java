import com.fasterxml.jackson.databind.ObjectMapper;
import model.OpenReqProject;
import model.OpenReqRequirement;
import model.OpenReqSchema;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
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
    private JPanel bm25fPanel;
    private JPanel fesvmPanel;
    private JLabel bm25fTitleLabel;

    private OpenReqSchema openReqSchema;

    public MainView() {
        initializeGUIComponents();
        setListeners();
        initializeTableModels();
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
        bm25fPanel.setVisible(false);
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
               bm25fPanel.setVisible(true);
               fesvmPanel.setVisible(false);
           }
       });
       fesvmCheckbox.addItemListener(e -> {
           int state = e.getStateChange();
           if (state == ItemEvent.SELECTED) {
               bm25fCheckbox.setSelected(false);
               fesvmPanel.setVisible(true);
               bm25fPanel.setVisible(false);
           }
       });
    }

    private void fillRequirementTables(File requirementsFile) {
        for (int i = requirementsTableModel.getRowCount() - 1; i >= 0; --i) {
            requirementsTableModel.removeRow(i);
        }

        try {
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(requirementsFile));

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

        /*ArrayList<String[]> projects = new ArrayList<String[]>();
        for (int i = 0; i < json.getJSONArray("requirements").length(); ++i) {
            JSONObject project = json.getJSONArray("requirements").getJSONObject(i);
            String[] projectData = {project.getString("id"), project.getString("text")};
            projects.add(projectData);
        }
        for (String[] project : projects) {
            reqTableModel.addRow(project);
        }*/

        requirementsTableModel.addRow(new String[]{"id", "name", "text"});

    }

    public static void main(String[] args) {
        JFrame jframe = new JFrame("Main view");
        jframe.setContentPane(new MainView().rootPanel);
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.pack();
        jframe.setVisible(true);
    }

}
