package ru.game.aurora.tools.dialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.game.aurora.dialog.Condition;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.Reply;
import ru.game.aurora.dialog.Statement;
import ru.game.aurora.tools.Context;
import ru.game.aurora.util.EngineUtils;

import java.io.*;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Converts excel-style ';'-delimieted csv into dialog
 * Each line is either dialog statement, or a reply
 * Statement:
 * ID;npc text;custom icon (if any)
 * Reply:
 * empty;reply text; target ID;condition;return value;flags
 * <p/>
 * If condition is set, it can be one of following: either a variable name, or a variable name sign value, like 'quest.value=4'
 */
public class DialogCSVConverter {
    private static final String delimiter = ";";

    private static Condition parseSingleCondition(String value) {
        String[] split = value.split("=");
        Condition condition;
        if (split.length == 1) {
            int gr = value.indexOf('>');
            int ls = value.indexOf('<');
            if (gr != -1) {
                condition = new Condition(value.substring(0, gr), value.substring(gr + 1), Condition.ConditionType.GREATER);
            } else if (ls != -1) {
                condition = new Condition(value.substring(0, ls), value.substring(ls + 1), Condition.ConditionType.LESS);
            } else if (split[0].startsWith("!")) {
                condition = new Condition(split[0].substring(1), null, Condition.ConditionType.NOT_SET);
            } else {
                condition = new Condition(split[0], null, Condition.ConditionType.SET);
            }
        } else {
            if (split[0].endsWith("!")) {
                condition = new Condition(split[0].substring(0, split[0].length() - 1), split[1], Condition.ConditionType.NOT_EQUAL);
            } else {
                condition = new Condition(split[0], split[1], Condition.ConditionType.EQUAL);
            }
        }

        return condition;
    }

    private static Condition[] parseConditions(String value) {
        String[] parts = value.split("&&");
        Condition[] result = new Condition[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            result[i] = parseSingleCondition(parts[i].trim());
        }
        return result;
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
            replies[i] = new Reply(
                    replyString.length >= 5 && !replyString[4].isEmpty() ? Integer.parseInt(replyString[4]) : 0
                    , Integer.parseInt(replyString[2])
                    , Integer.toString(i)
                    , replyString.length > 3 && !replyString[3].isEmpty() ? parseConditions(replyString[3]) : null
                    , replyString.length >= 6 ? parseFlags(replyString[5]) : null);
        }

        context.text.put(textId, stmtStrings[1]);
        if (replies.length == 0) {
            throw new IllegalStateException("Empty reply list at line " + context.lineNumber);
        }
        return new Statement(stmtId, stmtStrings.length > 2 ? stmtStrings[2] : null, null, replies);
    }

    private static Map<String, String> parseFlags(String s) {
        Map<String, String> flags = new HashMap<>();
        String[] values = s.split("=");
        if (values.length == 1) {
            flags.put(s, "true");
        } else {
            flags.put(values[0], values[1]);
        }
        return flags;
    }


    public static boolean process(String input, String output, File localizationRoot, String dialogId, String imageId) throws IOException {
        File inputFile = new File(input);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), EngineUtils.detectEncoding(input)))) {

            Map<Integer, Statement> statements = new HashMap<>();
            Context context = new Context(dialogId);
            System.out.println(String.format("Processing input %s with id %s", input, dialogId));
            String[] stmtLine = reader.readLine().split(delimiter);
            List<String[]> replyStrings = new ArrayList<>();
            while (true) {
                context.lineNumber++;
                try {
                    String line = reader.readLine();
                    if (line == null) {
                        if (stmtLine != null) {
                            Statement st = parseStatement(stmtLine, replyStrings, context);
                            statements.put(st.id, st);
                        }
                        break;
                    }

                    String[] parts = line.split(delimiter);
                    if (parts.length == 0) {
                        // skip empty line
                        System.err.println("You better delete empty line " + context.lineNumber);
                        continue;
                    }
                    if (parts[0].isEmpty()) {
                        // this is a reply
                        replyStrings.add(parts);
                        continue;
                    }

                    if (stmtLine != null) {
                        Statement st = parseStatement(stmtLine, replyStrings, context);
                        statements.put(st.id, st);
                    }
                    stmtLine = parts;
                    replyStrings.clear();
                } catch (Exception ex) {
                    System.err.println("Error parsing line " + context.lineNumber);
                    ex.printStackTrace();
                    return false;
                }

            }

            System.out.println("CSV parsed");

            Dialog dialog = new Dialog(dialogId, imageId, statements);

            System.out.println("Validating");

            if (!DialogValidator.validate(dialog)) {
                System.err.println("Validation failed, see log for details");
                return false;
            }

            File outDir = new File(output);
            if (!outDir.exists()) {
                boolean mkdirRz = outDir.mkdirs();
                if (!mkdirRz) {
                    throw new RuntimeException("Failed to create output dir, mkdirs() returned false");
                }
            }

            System.out.println("Saving structure");
            // save dialog structure file
            Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.STATIC).create();
            FileWriter writer = new FileWriter(new File(outDir, dialogId + ".json"));
            gson.toJson(dialog, writer);
            writer.close();

            System.out.println("Saving localization");
            // save localizations
            File locFile = localizationRoot == null ? new File(outDir, dialogId + "_ru.properties") : new File(localizationRoot, "ru/dialogs/" + dialogId + "_ru.properties");
            Writer localizationWriter = new OutputStreamWriter(new FileOutputStream(locFile), "utf-8");
            context.text.store(localizationWriter, "Autogenerated from " + inputFile.getName());
            localizationWriter.close();

            locFile = localizationRoot == null ? new File(outDir, dialogId + "_en.properties") : new File(localizationRoot, "en/dialogs/" + dialogId + "_en.properties");
            FileWriter enStubWriter = new FileWriter(locFile);
            enStubWriter.write("# Autogenerated from " + inputFile.getName() + "\n");
            for (Map.Entry<Object, Object> entry : context.text.entrySet()) {
                enStubWriter.write(entry.getKey() + "=TBD\n");
            }
            enStubWriter.close();
            System.out.println("All done");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 4) {
            System.err.println("Usage: DialogCSVConverter <input file> <dialog string id> <main image id> <out dir>");
            return;
        }

        File input = new File(args[0]);
        if (!input.exists()) {
            System.err.println("Input file does not exist");
            return;
        }

        process(args[0], args[3], null, args[1], args[2]);

    }
}
