package ru.game.aurora.tools.intro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringEscapeUtils;
import ru.game.aurora.dialog.IntroDialog;
import ru.game.aurora.tools.Context;
import ru.game.aurora.util.EngineUtils;

import java.io.*;
import java.util.Map;

/**
 * Creates internal data representation for an intro dialog based on given csv file
 * Csv file should have following format:
 * CAPTION;TEXT;PORTRAIT;MAIN IMAGE
 */

public class IntroCSVConverter {

    private static final String delimiter = ";";

    private static IntroDialog.Statement parseLine(Context context, String line) {
        String[] tokens = line.split(delimiter);

        if (tokens.length < 3) {
            System.err.println("Failed to convert line " + context.lineNumber);
            return null;
        }

        String captionId = null;
        if (!tokens[0].isEmpty()) {
            captionId = context.id + "." + context.lineNumber + ".caption";
            context.text.put(captionId, tokens[0]);
        }

        final String textId = context.id + "." + context.lineNumber + ".text";


        context.text.put(textId, StringEscapeUtils.unescapeCsv(tokens[2]));

        return new IntroDialog.Statement(tokens.length > 3 ? tokens[3] : null, captionId, tokens[1].isEmpty() ? null : tokens[1], textId);
    }


    private static Context<IntroDialog.Statement> readFile(File input, String id) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(input), EngineUtils.detectEncoding(input.getAbsolutePath())));
        reader.readLine(); // skip first line with headers
        String line = reader.readLine();

        Context<IntroDialog.Statement> context = new Context<>(id);
        System.out.println("Started parsing CSV");
        while (line != null) {
            context.lineNumber++;
            IntroDialog.Statement e = parseLine(context, line);
            if (e != null) {
                context.statements.add(e);
            }
            line = reader.readLine();
        }
        return context;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.err.println("Usage: IntroCSVConverter <input file> <intro string id> <main image id> <out dir>");
            return;
        }

        File input = new File(args[0]);
        if (!input.exists()) {
            System.err.println("Input file does not exist");
            return;
        }

        Context<IntroDialog.Statement> context = readFile(input, args[1]);
        Context<IntroDialog.Statement> englishContext = null;

        try {
            // check for english localization
            String[] split = args[0].split("\\.");
            File englishFile = new File(split[0] + "_eng." + split[1]);
            if (englishFile.exists()) {
                englishContext = readFile(englishFile, args[1]);
            } else {
                englishContext = context;
            }

            System.out.println("CSV parsed");
            IntroDialog introDialog = new IntroDialog(args[1], args[2], (IntroDialog.Statement[]) context.statements.toArray(new IntroDialog.Statement[context.statements.size()]));

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
            FileWriter localizationWriter = new FileWriter(new File(outDir, args[1] + "_ru.properties"));
            for (Map.Entry<Object, Object> entry : context.text.entrySet()) {
                localizationWriter.write(entry.getKey() + "=" + StringEscapeUtils.escapeJava(entry.getValue().toString()));
                localizationWriter.write('\n');
            }
            localizationWriter.close();

            localizationWriter = new FileWriter(new File(outDir, args[1] + "_en.properties"));
            for (Map.Entry<Object, Object> entry : englishContext.text.entrySet()) {
                localizationWriter.write(entry.getKey() + "=" + StringEscapeUtils.escapeJava(entry.getValue().toString()));
                localizationWriter.write('\n');
            }

            localizationWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
