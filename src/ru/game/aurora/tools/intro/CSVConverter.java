package ru.game.aurora.tools.intro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.game.aurora.dialog.IntroDialog;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Creates internal data representation for an intro dialog based on given csv file
 * Csv file should have following format:
 * CAPTION;TEXT;IMAGE
 */

public class CSVConverter {

    private static final String delimiter = ";";

    private static final class Context {
        public String id;

        public int lineNumber = 0;

        public Properties text = new Properties();

        private Context(String id) {
            this.id = id;
        }
    }

    private static IntroDialog.Statement parseLine(Context context, String line) {
        String[] tokens = line.split(delimiter);

        if (tokens.length != 3) {
            System.err.println("Failed to convert line " + context.lineNumber);
            return null;
        }

        final String captionId = context.id + "." + context.lineNumber + ".caption";
        final String textId = context.id + "." + context.lineNumber + ".text";

        context.text.put(captionId, tokens[0]);
        context.text.put(textId, tokens[1]);

        return new IntroDialog.Statement(captionId, tokens[2], textId);
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: CSVConverter <input file> <intro string id> <main image id> <out dir>");
            return;
        }

        File input = new File(args[0]);
        if (!input.exists()) {
            System.err.println("Input file does not exist");
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            reader.readLine(); // skip first line with headers
            String line = reader.readLine();

            List<IntroDialog.Statement> statements = new ArrayList<>();
            Context context = new Context(args[1]);
            System.out.println("Started parsing CSV");
            while (line != null) {
                context.lineNumber++;
                IntroDialog.Statement e = parseLine(context, line);
                if (e == null) {
                    continue;
                }
                statements.add(e);
                line = reader.readLine();
            }

            System.out.println("CSV parsed");
            IntroDialog introDialog = new IntroDialog(args[1], args[2], (IntroDialog.Statement[]) statements.toArray(new IntroDialog.Statement[statements.size()]));

            File outDir = new File(args[3]);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }

            System.out.println("Saving structure");
            // save dialog structure file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(new File(outDir, args[1] + ".json"));
            gson.toJson(introDialog, writer);
            writer.close();

            System.out.println("Saving localization");
            // save localizations
            FileWriter localizationWriter = new FileWriter(new File(outDir, args[1] + "_localization.properties"));
            context.text.store(localizationWriter, null);
            localizationWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
