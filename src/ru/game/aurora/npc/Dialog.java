/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 22:20
 */
package ru.game.aurora.npc;

import com.google.gson.Gson;
import de.matthiasmann.twl.Widget;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Dialog implements Room {

    public static class Statement implements Serializable {
        public final int id;

        public final String npcText;

        public final Reply[] replies;

        public Statement(int id, String npcText, Reply... replies) {
            this.id = id;
            this.npcText = npcText;
            this.replies = replies;
        }
    }

    public static class Reply implements Serializable {

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
    public Widget getGUI() {
        return null;
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

        if (idx >= currentStatement.replies.length || idx < 0) {
            return;
        }
        Reply selectedReply = currentStatement.replies[idx];
        currentStatement = statements.get(selectedReply.targetStatementId);
        returnValue = selectedReply.returnValue;
    }

    private static final Rectangle iconRectangle = new Rectangle(3, 3, 4, 4);
    private static final Rectangle npcStatementRectangle = new Rectangle(8, 3, 4, 4);
    private static final Rectangle replyRectangle = new Rectangle(3, 8, 9, 5);

    private static final Color backgroundColor = new Color(4, 7, 125);

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {

        graphics.drawImage(ResourceManager.getInstance().getImage(iconName), camera.getRelativeX((int) iconRectangle.getX()), camera.getRelativeY((int) iconRectangle.getY()));
        graphics.setColor(backgroundColor);
        EngineUtils.drawRectWithBorder(graphics, npcStatementRectangle, camera, Color.yellow, backgroundColor);
        EngineUtils.drawRectWithBorder(graphics, replyRectangle, camera, Color.yellow, backgroundColor);

        graphics.setColor(Color.yellow);

        EngineUtils.drawRect(graphics, iconRectangle, camera, false);

        EngineUtils.drawString(
                graphics
                , currentStatement.npcText
                , camera.getRelativeX((int) npcStatementRectangle.getX()) + camera.getTileWidth() / 2
                , camera.getRelativeY((int) npcStatementRectangle.getY()) + camera.getTileHeight() / 2
                , camera.getTileWidth() * ((int) npcStatementRectangle.getWidth() - 1)
                , GUIConstants.dialogFont
                , Color.yellow);

        int i = 0;
        for (Reply r : currentStatement.replies) {
            graphics.drawString(
                    (i + 1) + ": " + r.replyText
                    , camera.getRelativeX((int) replyRectangle.getX()) + camera.getTileWidth() / 2
                    , camera.getRelativeY((int) (replyRectangle.getY() + (i++))) + camera.getTileHeight() / 2
            );
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
