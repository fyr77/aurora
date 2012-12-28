/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 22:20
 */
package ru.game.aurora.npc;

import com.google.gson.Gson;
import jgame.JGColor;
import jgame.JGRectangle;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.util.JGEngineUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class Dialog implements Room {

    public static class Statement {
        public final int id;

        public final String npcText;

        public final Reply[] replies;

        public Statement(int id, String npcText, Reply... replies) {
            this.id = id;
            this.npcText = npcText;
            this.replies = replies;
        }
    }

    public static class Reply {

        /**
         * If this reply will be the last action of the dialog, this will be that dialog's return value
         */
        public final int returnValue;

        public final int targetStatementId;

        public final String replyText;

        public Reply(int returnValue, int targetStatementId, String replyText) {
            this.returnValue = returnValue;
            this.targetStatementId = targetStatementId;
            this.replyText = replyText;
        }
    }

    private String iconName;

    private Map<Integer, Statement> statements = new HashMap<Integer, Statement>();

    private Statement currentStatement;

    private int returnValue = 0;

    public Dialog() {
        // for gson
    }

    public Dialog(String iconName, Statement... statements) {
        this.iconName = iconName;
        for (Statement s : statements) {
            this.statements.put(s.id, s);
        }
    }

    public Dialog(String iconName, Map<Integer, Statement> statements) {
        this.iconName = iconName;
        this.statements = statements;
    }

    @Override
    public void enter(World world) {
        currentStatement = statements.get(0);
        returnValue = 0;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (currentStatement == null) {
            return;
        }

        int idx = -1;
        for (int i = Input.KEY_1; i < Input.KEY_9; ++i) {
            if (container.getInput().isKeyDown(i)) {
                idx = i - 1;
                break;
            }
        }

        if (idx >= currentStatement.replies.length || idx < 0) {
            return;
        }
        Reply selectedReply = currentStatement.replies[idx];
        currentStatement = statements.get(selectedReply.targetStatementId);
        returnValue = selectedReply.returnValue;
    }

    private static final JGRectangle iconRectangle = new JGRectangle(3, 3, 4, 4);
    private static final JGRectangle npcStatementRectangle = new JGRectangle(8, 3, 4, 4);
    private static final JGRectangle replyRectangle = new JGRectangle(3, 8, 9, 5);

    private static final JGColor backgroundColor = new JGColor(4, 7, 125);

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {

        graphics.drawImage(iconName, camera.getRelativeX(iconRectangle.x), camera.getRelativeY(iconRectangle.y));
        engine.setColor(backgroundColor);
        JGEngineUtils.drawRectWithBorder(engine, npcStatementRectangle, camera, JGColor.yellow, backgroundColor);
        JGEngineUtils.drawRectWithBorder(engine, replyRectangle, camera, JGColor.yellow, backgroundColor);

        engine.setColor(JGColor.yellow);

        JGEngineUtils.drawRect(engine, iconRectangle, camera, false);

        JGEngineUtils.drawString(
                engine
                , currentStatement.npcText
                , camera.getRelativeX(npcStatementRectangle.x) + camera.getTileWidth() / 2
                , camera.getRelativeY(npcStatementRectangle.y) + camera.getTileHeight() / 2
                , camera.getTileWidth() * (npcStatementRectangle.width - 1)
                , GUIConstants.dialogFont
                , JGColor.yellow);

        int i = 0;
        for (Reply r : currentStatement.replies) {
            engine.drawString(
                    (i + 1) + ": " + r.replyText
                    , camera.getRelativeX(replyRectangle.x) + camera.getTileWidth() / 2
                    , camera.getRelativeY(replyRectangle.y + (i++)) + camera.getTileHeight() / 2
                    ,
                    -1
                    , GUIConstants.dialogFont, JGColor.yellow);
        }
    }

    public boolean isOver() {
        return currentStatement == null;
    }

    public static Dialog loadFromFile(InputStream is) {
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(is);
        Dialog d = gson.fromJson(reader, Dialog.class);
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
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
