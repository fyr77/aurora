package ru.game.aurora.tools.dialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchDialogConverterGUI {
    
    private static Preferences userPrefs = Preferences.userRoot().node("AuroraRL").node("DialogConverter");
    
    private static final Logger logger = LoggerFactory.getLogger(BatchDialogConverterGUI.class);

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(465, 315);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        frame.add(panel);

        // input stuff
        JLabel inputLabel = new JLabel("Input directory:");
        inputLabel.setBounds(5, 5, 150, 20);
        panel.add(inputLabel);

        JTextField inputTextField = new JTextField(userPrefs.get("inputDirectory", "doc/dialog sources"));
        inputTextField.setBounds(5, 25, 200, 30);
        inputTextField.setEditable(false);
        panel.add(inputTextField);

        JButton inputButton = new JButton("Change");
        inputButton.setBounds(5, 55, 100, 30);
        inputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dialog = new JFileChooser(new File(inputTextField.getText()));
                dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                dialog.showOpenDialog(panel);

                File file = dialog.getSelectedFile();
                if (file != null) {
                    inputTextField.setText(file.getAbsolutePath());
                }
            }
        });
        panel.add(inputButton);

        // output stuff
        JLabel outputLabel = new JLabel("Output directory:");
        outputLabel.setBounds(225, 5, 150, 20);
        panel.add(outputLabel);

        JTextField outputTextField = new JTextField(userPrefs.get("outputDirectory", "resources"));
        outputTextField.setBounds(225, 25, 200, 30);
        outputTextField.setEditable(false);
        panel.add(outputTextField);

        JButton outputButton = new JButton("Change");
        outputButton.setBounds(225, 55, 100, 30);
        outputButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser dialog = new JFileChooser(new File(outputTextField.getText()));
                dialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                dialog.showOpenDialog(panel);

                File file = dialog.getSelectedFile();
                if (file != null) {
                    outputTextField.setText(file.getAbsolutePath());
                }
            }
        });
        panel.add(outputButton);

        // log console
        JTextArea textArea = new JTextArea();
        textArea.setText("Log will be here...");
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(5, 90, 440, 150);
        panel.add(scrollPane);

        PrintStream printStream = new PrintStream(new OutputStream() {

            @Override
            public void write(int b) throws IOException {
                textArea.append(String.valueOf((char) b));
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }

        });
        System.setOut(printStream);
        System.setErr(printStream);

        // convert button
        JButton convertButton = new JButton("Convert");
        convertButton.setBounds(170, 243, 100, 30);
        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                convertButton.setEnabled(false);
                textArea.setText("");

                userPrefs.put("inputDirectory", inputTextField.getText());
                userPrefs.put("outputDirectory", outputTextField.getText());

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BatchDialogConverter converter = new BatchDialogConverter(
                                new File(outputTextField.getText() + "/localization"),
                                new File(outputTextField.getText() + "/dialogs"));
                        try {
                            converter.processDialogsInFolder(new File(inputTextField.getText()));
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        
                        convertButton.setEnabled(true);
                        logger.info("Errors: " + converter.errorCount);
                    }
                });

                thread.start();
            }
        });
        panel.add(convertButton);

        frame.setResizable(false);
        frame.setVisible(true);
    }

}
