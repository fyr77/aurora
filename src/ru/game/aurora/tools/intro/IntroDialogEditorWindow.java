package ru.game.aurora.tools.intro;

import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.tools.workspace.AuroraWorkspace;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 05.12.13
 * Time: 17:18
 */
public class IntroDialogEditorWindow extends JFrame implements ActionListener, ItemListener {
    private IntroDialogModel model = new IntroDialogModel();

    private JButton addStatementButton;

    private JButton deleteStatementButton;

    private JComboBox<String> mainImageIds;

    private JPanel mainImagePanel;

    private JPanel portraitPanel;

    private JComboBox<String> portraitIds;

    private JTabbedPane tabbedPane;

    private AuroraWorkspace workspace;

    private JList<IntroDialog.Statement> statements;

    private File modelFile = null;

    public IntroDialogEditorWindow(AuroraWorkspace workspace) {
        this.workspace = workspace;

        setTitle("Intro dialog editor");
        createMenu();

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
        statements = new JList<>(model);
        statements.setMinimumSize(new Dimension(300, 500));
        leftPanel.add(statements);

        leftPanel.add(Box.createVerticalGlue());
        addStatementButton = new JButton("Add frame");
        addStatementButton.addActionListener(this);
        leftPanel.add(addStatementButton);
        deleteStatementButton = new JButton("Delete frame");
        deleteStatementButton.addActionListener(this);
        leftPanel.add(deleteStatementButton);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));

        rightPanel.add(new JLabel("Main image id:"));
        mainImageIds = new JComboBox<>(workspace.getImageIDs());
        mainImageIds.setMaximumSize(new Dimension(300, 100));
        rightPanel.add(mainImageIds);
        mainImagePanel = new JPanel();
        mainImagePanel.setMinimumSize(new Dimension(800, 300));
        mainImagePanel.setBorder(BorderFactory.createEtchedBorder());
        rightPanel.add(mainImagePanel);

        rightPanel.add(new JSeparator(SwingConstants.HORIZONTAL));

        JPanel framePanel = new JPanel();
        framePanel.setLayout(new BoxLayout(framePanel, BoxLayout.LINE_AXIS));

        JPanel dialogIconPanel = new JPanel();
        dialogIconPanel.setLayout(new BoxLayout(dialogIconPanel, BoxLayout.PAGE_AXIS));
        portraitPanel = new JPanel();
        portraitPanel.setBorder(BorderFactory.createEtchedBorder());
        portraitPanel.setMinimumSize(new Dimension(256, 256));
        dialogIconPanel.add(new JLabel("Statement icon"));
        portraitIds = new JComboBox<>(workspace.getImageIDs());
        dialogIconPanel.add(portraitIds);
        dialogIconPanel.add(portraitPanel);

        framePanel.add(dialogIconPanel);
        tabbedPane = new JTabbedPane();
        framePanel.add(tabbedPane);

        tabbedPane.addTab("ru", createTextTab());
        tabbedPane.addTab("en", createTextTab());

        rightPanel.add(framePanel);


        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(leftPanel), new JScrollPane(rightPanel));
        getContentPane().add(splitPane);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(800, 600);
        setLocationRelativeTo(null);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        fileMenu.addItemListener(this);

        JMenuItem newIntro = new JMenuItem();
        newIntro.setAction(new AbstractAction("New") {

            @Override
            public void actionPerformed(ActionEvent e) {
                model = new IntroDialogModel();
                statements.setModel(model);
            }
        });
        fileMenu.add(newIntro);

        JMenuItem open = new JMenuItem();
        open.setAction(new AbstractAction("Open...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(modelFile != null ? modelFile.getParentFile() : null);
                int rz = fileChooser.showOpenDialog(IntroDialogEditorWindow.this);
                if (rz == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    try {
                        model = IntroDialogModel.load(f);
                        modelFile = f;
                        statements.setModel(model);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(IntroDialogEditorWindow.this, "Failed to load intro : " + ex.getMessage(), "Load failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(open);

        fileMenu.add(new JMenuItem(new AbstractAction("Save As...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(modelFile != null ? modelFile.getParentFile() : null);
                int rz = fileChooser.showSaveDialog(IntroDialogEditorWindow.this);
                if (rz == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    try {
                        model.save(f);
                        modelFile = f;
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(IntroDialogEditorWindow.this, "Failed to save intro : " + ex.getMessage(), "Save failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }));


        fileMenu.add(new JMenuItem(new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (modelFile != null) {
                        model.save(modelFile);
                        return;
                    }
                    JFileChooser fileChooser = new JFileChooser();
                    int rz = fileChooser.showSaveDialog(IntroDialogEditorWindow.this);
                    if (rz == JFileChooser.APPROVE_OPTION) {
                        File f = fileChooser.getSelectedFile();

                        model.save(f);
                        modelFile = f;

                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(IntroDialogEditorWindow.this, "Failed to save intro : " + ex.getMessage(), "Save failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));
        JMenuItem exit = new JMenuItem();
        exit.setAction(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                IntroDialogEditorWindow.this.dispatchEvent(new WindowEvent(IntroDialogEditorWindow.this, WindowEvent.WINDOW_CLOSING));
            }
        });
        fileMenu.add(exit);

        setJMenuBar(menuBar);
    }

    private Component createTextTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel("Caption:"));
        panel.add(new JTextField());
        panel.add(new JLabel("Text:"));
        panel.add(new JTextArea());
        return panel;
    }

    private void update() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addStatementButton) {
            model.addStatement();
        } else if (e.getSource().equals(deleteStatementButton)) {
            int idx = statements.getSelectedIndex();
            if (idx >= 0) {
                model.deleteStatement(idx);
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

    }
}
