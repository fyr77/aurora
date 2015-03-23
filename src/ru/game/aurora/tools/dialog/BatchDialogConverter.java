/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 22.04.14
 * Time: 21:17
 */

package ru.game.aurora.tools.dialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.util.EngineUtils;

import java.io.*;

// csv format is
// input file name;id;portrait;output dir relative to output root
public class BatchDialogConverter {
    private final File outputRoot;

    private final File outputLocalizationRoot;

    private static final Logger logger = LoggerFactory.getLogger(BatchDialogConverter.class);

    private static final String CONFIG_NAME = "dialogList.csv";

    private int errorCount = 0;

    public BatchDialogConverter(File outputLocalizationRoot, File outputRoot) {
        this.outputLocalizationRoot = outputLocalizationRoot;
        this.outputRoot = outputRoot;
    }

    private void readAndProcessConfig(File currentFolder, File config) throws IOException {
        logger.info("Processing config" + config.getAbsolutePath());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(config), EngineUtils.detectEncoding(config)))) {

            String line;
            int idx = 0;
            do {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                String[] parts = line.split(";");
                if (parts.length != 4) {
                    logger.info("Malformed line " + idx);
                    ++idx;
                    ++errorCount;
                    continue;
                }

                final File inputFile = new File(currentFolder, parts[0]);
                final String portrait = parts[2];
                final String id = parts[1];
                final File outputDir = new File(outputRoot, parts[3]);

                if (portrait.isEmpty() || id.isEmpty()) {
                    logger.error("Malformed line " + idx);
                    ++errorCount;
                    ++idx;
                    continue;
                }

                if (!DialogCSVConverter.process(inputFile.getAbsolutePath(), outputDir.getAbsolutePath(), outputLocalizationRoot, id, portrait)) {
                    errorCount++;
                }

            } while (true);

        }

    }

    public void processDialogsInFolder(File folder) throws IOException {
        logger.info("Entering directory " + folder);
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                processDialogsInFolder(f);
            }

            if (f.getName().equalsIgnoreCase(CONFIG_NAME)) {
                readAndProcessConfig(folder, f);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BatchDialogConverter converter = new BatchDialogConverter(new File("resources/localization"), new File("resources/dialogs"));
        converter.processDialogsInFolder(new File("doc/dialog sources"));
        logger.info("Errors: " + converter.errorCount);
    }
}
