/**
 * User: jedi-philosopher
 * Date: 11.12.12
 * Time: 22:20
 */
package ru.game.aurora.dialog;

import com.google.gson.Gson;
import de.lessvoid.nifty.screen.Screen;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dialog implements OverlayWindow {

    private static final long serialVersionUID = -3952133424974552884L;

    private String iconName;

    private Map<Integer, Statement> statements = new HashMap<Integer, Statement>();

    private Statement currentStatement;

    private int returnValue = 0;

    /**
     * Replies that are available for current statement based on current world state.
     */
    private List<Reply> availableReplies;

    private DialogListener listener;

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

    public void setListener(DialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void enter(World world) {
        currentStatement = statements.get(0);
        availableReplies = currentStatement.getAvailableReplies(world);
        returnValue = 0;
    }

    @Override
    public Screen getGUI() {
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
        int lineIdx = 0;
        for (Reply r : availableReplies) {
            lineIdx += EngineUtils.drawString(
                    graphics
                    , (i + 1) + ": " + r.replyText
                    , camera.getRelativeX((int) replyRectangle.getX()) + camera.getTileWidth() / 2
                    , camera.getRelativeY((int) (replyRectangle.getY())) + camera.getTileHeight() / 2 + (lineIdx) * GUIConstants.dialogFont.getLineHeight()
                    , camera.getTileWidth() * ((int) replyRectangle.getWidth() - 1)
                    , GUIConstants.dialogFont
                    , Color.yellow
            );
            ++i;
            ++lineIdx;
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
