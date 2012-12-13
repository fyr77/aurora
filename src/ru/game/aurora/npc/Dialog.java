/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 22:20
 */
package ru.game.aurora.npc;

import com.google.gson.Gson;
import jgame.JGColor;
import jgame.JGRectangle;
import jgame.platform.JGEngine;
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
        public final int targetStatementId;

        public final String replyText;

        public Reply(int targetStatementId, String replyText) {
            this.targetStatementId = targetStatementId;
            this.replyText = replyText;
        }
    }

    private String iconName;

    private Map<Integer, Statement> statements = new HashMap<Integer, Statement>();

    private Statement currentStatement;

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
    }

    @Override
    public void update(JGEngine engine, World world) {
        if (currentStatement == null) {
            return;
        }
        char c = engine.getLastKeyChar();
        int idx = c - '1';
        if (idx >= currentStatement.replies.length || idx < 0) {
            return;
        }

        currentStatement = statements.get(currentStatement.replies[idx].targetStatementId);
    }

    private static final JGRectangle iconRectangle = new JGRectangle(3, 3, 4, 4);
    private static final JGRectangle npcStatementRectangle = new JGRectangle(8, 3, 4, 4);
    private static final JGRectangle replyRectangle = new JGRectangle(3, 8, 9, 5);

    private static final JGColor backgroundColor = new JGColor(4, 7, 125);

    @Override
    public void draw(JGEngine engine, Camera camera) {

        engine.drawImage(iconName, camera.getRelativeX(iconRectangle.x), camera.getRelativeY(iconRectangle.y));
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

    public static Dialog loadFromFile(InputStream is) throws IOException {
        Gson gson = new Gson();
        Reader reader = new InputStreamReader(is);
        Dialog d = gson.fromJson(reader, Dialog.class);
        reader.close();
        return d;
    }

}
