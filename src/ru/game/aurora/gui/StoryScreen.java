/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.01.13
 * Time: 17:53
 */
package ru.game.aurora.gui;

import com.google.gson.Gson;
import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;

/**
 * Screen that contains image, text and left/right buttons
 */
public class StoryScreen implements OverlayWindow {

    private static final long serialVersionUID = -3923197071117233494L;

    public static class StoryElement implements Serializable {
        private static final long serialVersionUID = 3916693966824352564L;

        public final String imageId;

        public final String text;

        public StoryElement(String imageId, String text) {
            this.imageId = imageId;
            this.text = text;
        }
    }

    private static final Gson gson = new Gson();

    private StoryElement[] screens;

    private int currentScreen;

    private final static Rectangle textRect = new Rectangle(1, 6, 12, 7);

    private DialogListener listener;

    public StoryScreen(String descPath) {
        Reader reader = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(descPath));
        screens = gson.fromJson(reader, StoryElement[].class);
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setListener(DialogListener listener) {
        this.listener = listener;
    }

    @Override
    public void enter(World world) {
        currentScreen = 0;
    }

    @Override
    public Screen getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_LEFT) && currentScreen > 0) {
            currentScreen--;
        }

        if ((container.getInput().isKeyPressed(Input.KEY_RIGHT) || container.getInput().isKeyPressed(Input.KEY_ENTER)) && currentScreen < screens.length) {
            currentScreen++;
            if (isOver() && listener != null) {
                listener.onDialogEnded(world, 0);
            }
        }

        GameLogger.getInstance().addStatusMessage("Press <left> or <right> to change pages");
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        if (isOver()) {
            return;
        }
        graphics.clear();
        final Image image = ResourceManager.getInstance().getImage(screens[currentScreen].imageId);
        graphics.drawImage(image, 64, 64);
        graphics.setColor(Color.yellow);
        graphics.drawRect(64, 64, image.getWidth(), image.getHeight());
        EngineUtils.drawRectWithBorderAndText(graphics, textRect, camera, Color.yellow, GUIConstants.backgroundColor, screens[currentScreen].text, GUIConstants.dialogFont, Color.white, false);

    }

    public boolean isOver() {
        return currentScreen == screens.length;
    }

}
