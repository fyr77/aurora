package ru.game.aurora.tools.dialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.game.aurora.dialog.Condition;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.tools.Context;

import java.io.*;
import java.util.*;

/**
 * Converts excel-style ';'-delimieted csv into dialog
 * Each line is either dialog statement, or a reply
 * Statement:
 * ID;npc text;custom icon (if any)
 * Reply:
 * empty;reply text; target ID;condition;return value
 *
 * If condition is set, it can be one of following: either a variable name, or a variable name sign value, like 'quest.value=4'
 */
public class DialogCSVConverter
{
    private static final String delimiter = ";";

    private static Condition[] parseConditions(String value)
    {
        String[] split = value.split("=");
        Condition[] conditions = new Condition[1];
        if (split.length == 1) {
            // this is either SET or NOT_SET
            if (split[0].startsWith("!")) {
                conditions[0] = new Condition(split[0].substring(1), null, Condition.ConditionType.NOT_SET);
            } else {
                conditions[0] = new Condition(split[0], null, Condition.ConditionType.SET);
            }
        } else {
            conditions[0] = new Condition(split[0], split[1], Condition.ConditionType.EQUAL);
        }
        return conditions;
    }

    private static Statement parseStatement(String[] stmtStrings, List<String[]> replyStrings, Context context) throws IOException {
        if (stmtStrings == null) {
            throw new IllegalArgumentException();
        }

        int stmtId = Integer.parseInt(stmtStrings[0]);

        if (stmtStrings.length < 2 || stmtStrings.length > 3) {
            System.err.println("Invalid statement format in line " + context.lineNumber + ": " + Arrays.toString(stmtStrings));
            return null;
        }
        final String textId = context.id + "." + stmtId;
        Reply[] replies = new Reply[replyStrings.size()];
        for (int i = 0; i < replyStrings.size(); ++i) {
            String[] replyString = replyStrings.get(i);
            final String replyTextId = textId + "." + i;
            context.text.put(replyTextId, replyString[1]);
            replies[i] = new Reply(replyString.length >= 5 ? Integer.parseInt(replyString[4]) : 0, Integer.parseInt(replyString[2]), replyTextId, replyString.length > 3 ? parseConditions(replyString[3]) : null);
        }

        context.text.put(textId, stmtStrings[1]);
        return new Statement(stmtId, stmtStrings.length > 2 ? stmtStrings[2] : null, textId, replies);
    }

    public static void main(String[] args)
    {
        if (args.length != 4) {
            System.err.println("Usage: DialogCSVConverter <input file> <dialog string id> <main image id> <out dir>");
            return;
        }

        File input = new File(args[0]);
        if (!input.exists()) {
            System.err.println("Input file does not exist");
            return;
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(input));
            Map<Integer, Statement> statements = new HashMap<>();
            Context context = new Context(args[1]);
            System.out.println("Started parsing CSV");
            String[] stmtLine = reader.readLine().split(delimiter);
            List<String[]> replyStrings = new ArrayList<>();
            while (true) {
                context.lineNumber++;
                String line = reader.readLine();
                if (line == null) {
                    if (stmtLine != null) {
                        Statement st = parseStatement(stmtLine, replyStrings, context);
                        statements.put(st.id, st);
                    }
                    break;
                }

                String[] parts = line.split(delimiter);
                if (parts[0].isEmpty()) {
                    // this is a reply
                    replyStrings.add(parts);
                    continue;
                }

                if (stmtLine != null) {
                    Statement st = parseStatement(stmtLine, replyStrings, context);
                    statements.put(st.id, st);
                }
                stmtLine = null;
                replyStrings.clear();

            }

            System.out.println("CSV parsed");

            Dialog dialog = new Dialog(args[1], args[2], statements);

            File outDir = new File(args[3]);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }

            System.out.println("Saving structure");
            // save dialog structure file
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(new File(outDir, args[1] + ".json"));
            gson.toJson(dialog, writer);
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
