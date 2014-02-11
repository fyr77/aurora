package ru.game.aurora.tools.dialog;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.prefs.Preferences;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 10.01.14
 * Time: 23:15
 */
public class DialogCSVConverterGUI extends JFrame implements ActionListener {
    private static final long serialVersionUID = -8047639658518892046L;

    private JTextField source;

    private JTextField out;

    private JTextField id;

    private JTextField image;

    private JButton sourceBrowseButton;

    private JButton targetBrowseButton;

    private JButton processButton;

    private JButton clearLogButton;

    private JTextArea log;

    private static final String PREV_SOURCE_PATH_KEY = "sourcePath";

    private static final String PREV_OUT_PATH_KEY = "outPath";

    private static final String PREV_ID_KEY = "prevId";

    private static final String PREV_IMAGE_KEY = "prevImage";

    private transient Preferences prefs = Preferences.userNodeForPackage(DialogCSVConverterGUI.class);

    private ByteArrayOutputStream bos = new ByteArrayOutputStream();

    public DialogCSVConverterGUI() throws FileNotFoundException {
        super("Aurora dialog converter");
        getContentPane().setLayout(new BorderLayout());

        createTopPanel();

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(new JLabel("Log:"));
        log = new JTextArea();
        log.setMinimumSize(new Dimension(200, 400));
        centerPanel.add(new JScrollPane(log));
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
        processButton = new JButton("Process");
        processButton.addActionListener(this);
        clearLogButton = new JButton("Clear output");
        clearLogButton.addActionListener(this);
        bottomPanel.add(clearLogButton);
        bottomPanel.add(Box.createHorizontalStrut(10));
        bottomPanel.add(processButton);
        add(bottomPanel, BorderLayout.PAGE_END);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        id.setText(prefs.get(PREV_ID_KEY, ""));
        image.setText(prefs.get(PREV_IMAGE_KEY, ""));
        setSize(600, 650);

    }

    private void createTopPanel() throws FileNotFoundException {
        JPanel topPanel = new JPanel();
        topPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));

        JPanel sourcePanel = new JPanel();
        topPanel.add(new JLabel("Choose source .csv file:"));
        sourcePanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.LINE_AXIS));
        source = new JTextField(48);
        sourcePanel.add(source);
        sourceBrowseButton = new JButton("...");
        sourceBrowseButton.addActionListener(this);
        sourcePanel.add(sourceBrowseButton);
        sourcePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        sourcePanel.add(Box.createHorizontalBox());
        topPanel.add(sourcePanel);

        JPanel outputPanel = new JPanel();
        topPanel.add(new JLabel("Choose output dir"));
        outputPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        outputPanel.setLayout(new BoxLayout(outputPanel, BoxLayout.LINE_AXIS));
        out = new JTextField(48);
        String prevPath = prefs.get(PREV_OUT_PATH_KEY, null);
        if (prevPath != null) {
            out.setText(prevPath);
        }
        outputPanel.add(out);
        targetBrowseButton = new JButton("...");
        targetBrowseButton.addActionListener(this);
        outputPanel.add(targetBrowseButton);
        outputPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        // outputPanel.add(Box.createHorizontalGlue());
        topPanel.add(outputPanel);

        topPanel.add(new JLabel("Dialog id:"));
        id = new JTextField(24);
        topPanel.add(id);
        topPanel.add(new JLabel("Image id:"));
        image = new JTextField(24);
        topPanel.add(image);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(topPanel, BorderLayout.PAGE_START);

        PrintStream logOut = new PrintStream(bos);
        System.setOut(logOut);
        System.setErr(logOut);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sourceBrowseButton) {
            JFileChooser chooser;
            String prevPath = prefs.get(PREV_SOURCE_PATH_KEY, null);
            if (prevPath != null) {
                chooser = new JFileChooser(prevPath);
            } else {
                chooser = new JFileChooser();
            }
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().endsWith(".csv") || f.getName().endsWith(".CSV");
                }

                @Override
                public String getDescription() {
                    return "CSV files";
                }
            });
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                source.setText(chooser.getSelectedFile().getAbsolutePath());
                prefs.put(PREV_SOURCE_PATH_KEY, chooser.getSelectedFile().getParent());
            }
        } else if (e.getSource() == targetBrowseButton) {
            JFileChooser chooser;
            String prevPath = prefs.get(PREV_OUT_PATH_KEY, null);
            if (prevPath != null) {
                chooser = new JFileChooser(prevPath);
            } else {
                chooser = new JFileChooser();
            }
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "Output directory";
                }
            });
            if (chooser.showDialog(this, "Select") == JFileChooser.APPROVE_OPTION) {
                out.setText(chooser.getSelectedFile().getAbsolutePath());
                prefs.put(PREV_OUT_PATH_KEY, chooser.getSelectedFile().getAbsolutePath());
            }
        } else if (e.getSource() == processButton) {
            if (out.getText().isEmpty()
                    || source.getText().isEmpty()
                    || image.getText().isEmpty()
                    || id.getText().isEmpty()
                    ) {
                JOptionPane.showMessageDialog(this, "All fields must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            process();
        } else if (e.getSource() == clearLogButton) {
            bos.reset();
            log.setText(null);
        }
    }

    private void process() {
        prefs.put(PREV_ID_KEY, id.getText());
        prefs.put(PREV_IMAGE_KEY, image.getText());
        try {
            DialogCSVConverter.process(source.getText(), out.getText(), id.getText(), image.getText());
            log.setText(new String(bos.toByteArray()));
            invalidate();
            System.out.println("====================================");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error while processing", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        new DialogCSVConverterGUI().setVisible(true);
    }
}
