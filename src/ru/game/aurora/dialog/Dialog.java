/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 22:20
 */
package ru.game.aurora.dialog;

import com.google.gson.Gson;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.PlaceholderResolver;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

public class Dialog implements OverlayWindow {

    private static final Logger logger = LoggerFactory.getLogger(Dialog.class);

    private static final long serialVersionUID = 3L;

    private String id;

    private String iconName;

    private Map<Integer, Statement> statements = new HashMap<>();

    // replies can set these flags, which can later be checked
    private transient Map<String, String> flags = new HashMap<>();

    private transient Statement currentStatement;

    private transient int returnValue = 0;

    /**
     * Replies that are available for current statement based on current world state.
     */
    private List<Reply> availableReplies;

    private List<DialogListener> listeners;

    // dialog can start from different statements, based on world conditions
    private Map<Integer, Condition> firstStatements;

    public Dialog() {
        // for gson
    }

    public Dialog(String id, String iconName, Statement... statements) {
        this.id = id;
        this.iconName = iconName;
        for (Statement s : statements) {
            this.statements.put(s.id, s);
        }
    }

    public Dialog(String id, String iconName, Map<Integer, Statement> statements) {
        this.id = id;
        this.iconName = iconName;
        this.statements = statements;
    }

    public String getId() {
        return id;
    }

    public Dialog(String id, String iconName, Map<Integer, Statement> statements, Map<Integer, Condition> firstStatements) {
        this.id = id;
        this.iconName = iconName;
        this.statements = statements;
        this.firstStatements = firstStatements;
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
        return PlaceholderResolver.resolvePlaceholders(Localization.getText(hasCustomBundle ? s : "dialogs", id + "." + currentStatement.id), world.getGlobalVariables());
    }

    public List<String> addAvailableRepliesLocalized(World world) {
        List<Reply> replies = currentStatement.getAvailableReplies(world, flags);
        List<String> outList = new ArrayList<>(replies.size());
        final String s = "dialogs/" + id;
        boolean hasCustomBundle = Localization.bundleExists(s);
        for (Reply reply : replies) {
            outList.add(Localization.getText(hasCustomBundle ? s : "dialogs", id + "." + currentStatement.id + "." + reply.replyText));
        }
        return outList;
    }

    public void setFlags(Map<String, String> flags) {
        if (this.flags == null) {
            this.flags = new HashMap<>(flags);
        } else {
            this.flags.clear();
            this.flags.putAll(flags);
        }
    }

    public Map<String, String> getFlags() {
        return flags;
    }

    @Override
    public void enter(World world) {
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
    public void update(GameContainer container, World world) {
        if (currentStatement == null) {
            return;
        }

        int idx = -1;
        for (int i = Input.KEY_1; i < Input.KEY_9; ++i) {
            if (container.getInput().isKeyPressed(i)) {
                idx = i - Input.KEY_1;
                break;
            }
        }

        if (idx >= availableReplies.size() || idx < 0) {
            return;
        }
        useReply(world, idx);
    }

    public void useReply(World world, int idx) {
        availableReplies = currentStatement.getAvailableReplies(world, flags);
        Reply selectedReply = availableReplies.get(idx);
        currentStatement = statements.get(selectedReply.targetStatementId);
        if (selectedReply.returnValue != 0) {
            returnValue = selectedReply.returnValue;
        }
        if (selectedReply.flags != null) {
            flags.putAll(selectedReply.flags);
        }

        if (currentStatement != null) {
            availableReplies = currentStatement.getAvailableReplies(world, flags);
        } else {
            availableReplies = null;
        }
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Statement getCurrentStatement() {
        return currentStatement;
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {

    }

    @Override
    public boolean isOver() {
        return currentStatement == null;
    }

    public static Dialog loadFromFile(InputStream is) {
        if (is == null) {
            throw new IllegalArgumentException("Can not load dialog from null stream");
        }
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(is);
        Dialog d = gson.fromJson(reader, Dialog.class);
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read dialog", e);
        }
        return d;
    }

    public static Dialog loadFromFile(String path) {
        InputStream is = Dialog.class.getClassLoader().getResourceAsStream(path);
        if (is == null) {
            logger.error("Failed to load dialog from " + path + ", maybe it does not exist");
            throw new IllegalArgumentException();
        }
        logger.info("Reading dialog from " + path);
        return loadFromFile(is);
    }

    public int getReturnValue() {
        return returnValue;
    }

    public void putStatement(Statement stmt) {
        this.statements.put(stmt.id, stmt);
    }

    public List<DialogListener> getListeners() {
        return listeners != null ? listeners : Collections.<DialogListener>emptyList();
    }

    public Map<Integer, Statement> getStatements() {
        return statements;
    }
}
