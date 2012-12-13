/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 22:20
 */
package ru.game.aurora.npc;

import jgame.JGColor;
import jgame.JGFont;
import jgame.JGRectangle;
import jgame.platform.JGEngine;
import ru.game.aurora.application.Camera;
import ru.game.aurora.util.JGEngineUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

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

    private static final JGFont dialogFont = new JGFont("arial", JGFont.BOLD, 16);

    private String iconName;

    private Map<Integer, Statement> statements = new HashMap<Integer, Statement>();

    private Statement currentStatement;

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

    private void drawRect(JGEngine engine, JGRectangle rectangle, Camera camera, boolean filled) {
        engine.drawRect(camera.getXCoord(rectangle.x), camera.getYCoord(rectangle.y), rectangle.width * camera.getTileWidth(), rectangle.height * camera.getTileHeight(), filled, false);
    }

    @Override
    public void draw(JGEngine engine, Camera camera) {

        engine.drawImage(iconName, camera.getXCoord(iconRectangle.x), camera.getYCoord(iconRectangle.y));
        engine.setColor(new JGColor(4, 7, 125));
        drawRect(engine, npcStatementRectangle, camera, true);
        drawRect(engine, replyRectangle, camera, true);

        engine.setColor(JGColor.yellow);

        drawRect(engine, iconRectangle, camera, false);
        drawRect(engine, npcStatementRectangle, camera, false);
        drawRect(engine, replyRectangle, camera, false);

        JGEngineUtils.drawString(
                engine
                , currentStatement.npcText
                , camera.getXCoord(npcStatementRectangle.x) + camera.getTileWidth() / 2
                , camera.getYCoord(npcStatementRectangle.y) + camera.getTileHeight() / 2
                , camera.getTileWidth() * (npcStatementRectangle.width - 1)
                , dialogFont
                , JGColor.yellow);

        int i = 0;
        for (Reply r : currentStatement.replies) {
            engine.drawString(
                    (i + 1) + ": " + r.replyText
                    , camera.getXCoord(replyRectangle.x) + camera.getTileWidth() / 2
                    , camera.getYCoord(replyRectangle.y + (i++)) + camera.getTileHeight() / 2
                    ,
                    -1
                    , dialogFont, JGColor.yellow);
        }
    }

    public boolean isOver() {
        return currentStatement == null;
    }

}
