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
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Localization;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dialog implements OverlayWindow {

    private static final long serialVersionUID = 1L;

    private String id;

    private String iconName;

    private Map<Integer, Statement> statements = new HashMap<>();

    private Statement currentStatement;

    private int returnValue = 0;

    /**
     * Replies that are available for current statement based on current world state.
     */
    private List<Reply> availableReplies;

    private DialogListener listener;

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

    public Dialog(String id, String iconName, Map<Integer, Statement> statements, Map<Integer, Condition> firstStatements) {
        this.id = id;
        this.iconName = iconName;
        this.statements = statements;
        this.firstStatements = firstStatements;
    }

    public void setListener(DialogListener listener) {
        this.listener = listener;
    }

    public String getLocalizedNPCText() {
        if (currentStatement.npcText != null) {
            // manually set localized string
            return currentStatement.npcText;
        }
        return Localization.getText("dialogs", id + "." + currentStatement.id);
    }

    public List<String> addAvailableRepliesLocalized(World world) {
        List<Reply> replies = currentStatement.getAvailableReplies(world);
        List<String> outList = new ArrayList<>(replies.size());
        for (Reply reply : replies) {
            outList.add(Localization.getText("dialogs", id + "." + currentStatement.id + "." + reply.replyText));
        }
        return outList;
    }

    @Override
    public void enter(World world) {
        if (firstStatements == null || firstStatements.isEmpty()) {
            currentStatement = statements.get(0);
        } else {
            for (Map.Entry<Integer, Condition> conditionEntry : firstStatements.entrySet()) {
                if (conditionEntry.getValue().isMet(world)) {
                    currentStatement = statements.get(conditionEntry.getKey());
                }
            }
            if (currentStatement == null) {
                throw new IllegalStateException("Can not select any statement to start dialog for current world condition");
            }
        }
        availableReplies = currentStatement.getAvailableReplies(world);
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
        if (availableReplies == null) {
            availableReplies = currentStatement.getAvailableReplies(world);
        }
        Reply selectedReply = availableReplies.get(idx);
        currentStatement = statements.get(selectedReply.targetStatementId);
        returnValue = selectedReply.returnValue;

        if (currentStatement != null) {
            availableReplies = currentStatement.getAvailableReplies(world);
        } else {
            // dialog is over
            if (listener != null) {
                listener.onDialogEnded(world, returnValue);
            }
            availableReplies = null;
        }
    }

    public String getIconName() {
        return iconName;
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
        return loadFromFile(Dialog.class.getClassLoader().getResourceAsStream(path));
    }

    public int getReturnValue() {
        return returnValue;
    }

    public void putStatement(Statement stmt) {
        this.statements.put(stmt.id, stmt);
    }
}
