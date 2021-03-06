/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 22:20
 */
package ru.game.aurora.dialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.*;
import ru.game.aurora.world.ITileMap;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Modifier;
import java.util.*;

public class Dialog implements OverlayWindow {

    private static final Logger logger = LoggerFactory.getLogger(Dialog.class);

    private static final long serialVersionUID = 4L;

    private List<DialogListener> listeners;

    private String fileName;

    private transient String id;

    private transient String iconName;

    private transient Map<Integer, Statement> statements = null;

    // replies can set these flags, which can later be checked
    private transient Map<String, String> flags = new HashMap<>();

    private transient Statement currentStatement;

    private transient int returnValue = 0;

    /**
     * Replies that are available for current statement based on current world state.
     */
    private transient List<Reply> availableReplies;

    // dialog can start from different statements, based on world conditions
    private transient Map<Integer, Condition> firstStatements;

    public Dialog() {
        // for gson
    }

    public Dialog(String id, String iconName, Map<Integer, Statement> statements) {
        this.id = id;
        this.iconName = iconName;
        this.statements = statements;
    }

    public Dialog(String id, String iconName, Collection<Statement> statements) {
        this.id = id;
        this.iconName = iconName;
        this.statements = new TreeMap<>(); // use a sorted map so that statements are in a fixed order after saving to json
        for (Statement st : statements) {
            this.statements.put(st.id, st);
        }
    }

    public static Dialog loadFromFile(String path) {
        Dialog d = new Dialog();

        String overrided = ResourceManager.getOverridedResources().get(path);
        if (overrided != null) {
            d.load(overrided);
        } else {
            d.load(path);
        }

        return d;
    }

    public String getId() {
        return id;
    }

    public void addListener(DialogListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removeListener(DialogListener listener) {
        listeners.remove(listener);
    }

    public String getLocalizedNPCText(World world) {
        if (currentStatement.npcText != null) {
            // manually set localized string
            return currentStatement.npcText;
        }
        final String s = "dialogs/" + id;
        boolean hasCustomBundle = Localization.bundleExists(s);
        String text = PlaceholderResolver.resolvePlaceholders(Localization.getText(hasCustomBundle ? s : "dialogs", id + "." + currentStatement.id), world.getGlobalVariables());
        if (flags != null && !flags.isEmpty()) {
            text = PlaceholderResolver.resolvePlaceholders(text, flags);
        }

        return text;
    }

    public List<String> addAvailableRepliesLocalized(World world) {
        List<Reply> replies = currentStatement.getAvailableReplies(world, flags);
        List<String> outList = new ArrayList<>(replies.size());
        final String s = "dialogs/" + id;
        boolean hasCustomBundle = Localization.bundleExists(s);

        int index = 1;
        for (Reply reply : replies) {
            outList.add(index + ") " + Localization.getText(hasCustomBundle ? s : "dialogs", id + "." + currentStatement.id + "." + reply.replyText));
            ++index;
        }
        return outList;
    }

    public Map<String, String> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, String> flags) {
        if (this.flags == null) {
            this.flags = new HashMap<>(flags);
        } else {
            this.flags.clear();
            this.flags.putAll(flags);
        }
    }

    public void setCurrentStatement(int startStatementId) {
        if(statements.containsKey(startStatementId)){
            this.currentStatement = statements.get(startStatementId);
        }
        else{
            throw new IllegalStateException("\"" + fileName +"\" - Can not select any statement with id " + startStatementId);
        }
    }

    @Override
    public void enter(World world) {
        if (statements == null) {
            load(fileName);
        }
        if (firstStatements == null || firstStatements.isEmpty()) {
            currentStatement = statements.get(0);
        } else {
            for (Map.Entry<Integer, Condition> conditionEntry : firstStatements.entrySet()) {
                if (conditionEntry.getValue().isMet(world, flags)) {
                    currentStatement = statements.get(conditionEntry.getKey());
                }
            }
            if (currentStatement == null) {
                throw new IllegalStateException("Can not select any statement to start dialog for current world condition");
            }
        }
        if (flags == null) {
            flags = new HashMap<>();
        } else {
            flags.clear();
        }

        returnValue = 0;
    }

    @Override
    public void returnTo(World world) {
        enter(world);
    }

    @Override
    public ITileMap getMap() {
        return null;
    }

    @Override
    public double getTurnToDayRelation() {
        return 0;
    }

    public void useReply(World world, int idx) {
        if(currentStatement != null){
            availableReplies = currentStatement.getAvailableReplies(world, flags);

            if(idx < 0 || idx > availableReplies.size()){
                return;
            }
            else{
                Reply selectedReply = availableReplies.get(idx);
                currentStatement = statements.get(selectedReply.targetStatementId);
                if (selectedReply.returnValue != 0) {
                    returnValue = selectedReply.returnValue;
                }
                if (selectedReply.flags != null) {
                    processFlags(selectedReply.flags, idx);
                }

                if (currentStatement != null) {
                    availableReplies = currentStatement.getAvailableReplies(world, flags);
                } else {
                    availableReplies = null;
                }
            }
        }
    }

    private void processFlags(Map<String, String> selectedReplyFlag, int replyId) {
        for(Map.Entry<String, String> entry: selectedReplyFlag.entrySet()){
            // math operators check
            if(entry.getValue().indexOf("+") != -1 || entry.getValue().indexOf("-") != -1){
                String value = entry.getValue();

                // represented entry value as {operand, operation, operand, operation, ..., operation, operand} string array
                String [] expression = value.split("(?<=[-+])|(?=[-+])");

                // math expression length is always odd number {val, +, val}[lenght=3] or {val, +, val, -, val}[length=5]
                if(expression != null && expression.length >= 3  && expression.length % 2 == 1){ //
                    try{
                        // in cycle -> {...{{{value, operator, value}, operator, value}, operator, value}...}

                        int result = getOperand(expression[0]);
                        for(int i = 0; i < expression.length - 1; i += 2){
                            switch (expression[i+1]) {
                                case "+": result += getOperand(expression[i + 2]); break;
                                case "-": result -= getOperand(expression[i + 2]); break;
                                default: throw new NumberFormatException("Undefined arithmetic operation");
                            }
                        }

                        // result of math expression
                        String strResult = Integer.toString(result);
                        logger.info("\"{}\" dialog arithmetic expression result={}, statementId={}, replyId={}", fileName, strResult, currentStatement.id, replyId);
                        flags.put(entry.getKey(), strResult);
                    }
                    catch (NumberFormatException e) {
                        // calculating failed cause invalid one operand or operator
                        flags.put(entry.getKey(), entry.getValue());
                    }
                }
                else{
                    // this is not supported math expression (signs do not correspond to the mathematical expression)
                    flags.put(entry.getKey(), entry.getValue());
                }
            }
            else{
                // this is not supported math expression (no operator '+' or '-' in expression)
                flags.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private int getOperand(String str) throws NumberFormatException {
        str = str.trim();
        if(flags.containsKey(str)){
            return Integer.parseInt(flags.get(str).trim());
        }
        else{
            return Integer.parseInt(str);
        }
    }

    public String getIconName() {
        if (statements == null) {
            load(fileName);
        }
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Statement getCurrentStatement() {
        if (statements == null) {
            load(fileName);
        }
        return currentStatement;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void update(GameContainer container, World world) {

    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {

    }

    @Override
    public boolean isOver() {
        return currentStatement == null;
    }

    private void load(String path) {
        InputStream is = AuroraGame.getResourceAsStream(path);
        if (is == null) {
            logger.error("Failed to load dialog from " + path + ", maybe it does not exist");
            throw new IllegalArgumentException();
        }
        logger.info("Reading dialog from " + path);
        Gson gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();
        Reader reader = new InputStreamReader(is);
        Dialog d = gson.fromJson(reader, Dialog.class);
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read dialog", e);
        }

        statements = d.statements;
        id = d.id;
        iconName = d.iconName;

        currentStatement = statements.get(0);
        fileName = path;
    }

    public int getReturnValue() {
        return returnValue;
    }

    public void putStatement(Statement stmt) {
        if (this.statements == null) {
            if (this.fileName != null) {
                load(this.fileName);
            } else {
                this.statements = new HashMap<>();
            }
        }
        this.statements.put(stmt.id, stmt);
    }

    public List<DialogListener> getListeners() {
        return listeners != null ? listeners : Collections.<DialogListener>emptyList();
    }

    public Map<Integer, Statement> getStatements() {
        return statements;
    }
}
