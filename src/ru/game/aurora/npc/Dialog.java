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
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dialog implements OverlayWindow {

    private static final long serialVersionUID = -3952133424974552884L;

    public static class Statement implements Serializable {

        private static final long serialVersionUID = -9058694068037621906L;

        public final int id;

        public final String npcText;

        public final Reply[] replies;

        public Statement(int id, String npcText, Reply... replies) {
            this.id = id;
            this.npcText = npcText;
            this.replies = replies;
        }

        public List<Reply> getAvailableReplies(World world)
        {
            List<Reply> rz = new ArrayList<Reply>(replies.length);
            for (Reply r : replies) {
                if (r.isVisible(world)) {
                    rz.add(r);
                }
            }

            return rz;
        }
    }

    public static class Reply implements Serializable {

        private static final long serialVersionUID = -1616895998816949360L;
        /**
         * If this reply will be the last action of the dialog, this will be that dialog's return value
         */
        public final int returnValue;

        public final int targetStatementId;

        public final String replyText;

        /**
         * This reply will only be visible if global game state contains given global variables with given values
         */
        public final Map<String, String> replyConditions;

        public Reply(int returnValue, int targetStatementId, String replyText) {
            this.returnValue = returnValue;
            this.targetStatementId = targetStatementId;
            this.replyText = replyText;
            this.replyConditions = null;
        }

        public Reply(int returnValue, int targetStatementId, String replyText, Map<String, String> replyConditions) {
            this.returnValue = returnValue;
            this.targetStatementId = targetStatementId;
            this.replyText = replyText;
            this.replyConditions = replyConditions;
        }

        /**
         * Returns true if this dialog option is available given current world state
         */
        public boolean isVisible(World world)
        {
            if (replyConditions == null) {
                return true;
            }

            for (Map.Entry<String, String> replyCondition : replyConditions.entrySet()) {
                if (!world.getGlobalVariables().containsKey(replyCondition.getKey())) {
                    return false;
                }

                String val = world.getGlobalVariables().get(replyCondition.getKey());
                String desiredVal = replyCondition.getValue();

                if ((val != null && !val.equals(desiredVal)) || (val == null && desiredVal != null)) {
                    return false;
                }
            }
            return true;
        }
    }

    private String iconName;

    private Map<Integer, Statement> statements = new HashMap<Integer, Statement>();

    private Statement currentStatement;

    private int returnValue = 0;

    /**
     * Replies that are available for current statement based on current world state.
     */
    private List<Reply> availableReplies;

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
        availableReplies = currentStatement.getAvailableReplies(world);
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

        if (idx >= availableReplies.size() || idx < 0) {
            return;
        }
        Reply selectedReply = availableReplies.get(idx);
        currentStatement = statements.get(selectedReply.targetStatementId);
        if (currentStatement != null) {
            availableReplies = currentStatement.getAvailableReplies(world);
        } else {
            availableReplies = null;
        }
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
        for (Reply r : availableReplies) {
            graphics.drawString(
                    (i + 1) + ": " + r.replyText
                    , camera.getRelativeX((int) replyRectangle.getX()) + camera.getTileWidth() / 2
                    , camera.getRelativeY((int) (replyRectangle.getY() + (i++))) + camera.getTileHeight() / 2
            );
        }
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
