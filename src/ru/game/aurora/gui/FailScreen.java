/**
 * User: jedi-philosopher
 * Date: 27.01.13
 * Time: 16:01
 */
package ru.game.aurora.gui;

import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.GUIConstants;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

/**
 * Screen that is shown on gameover. Shows picture and message, returns to main menu after closed
 */
public class FailScreen implements OverlayWindow {
    private final String text;

    private final String image;

    private final static Rectangle textRect = new Rectangle(1, 7, 11, 5);

    private static final long serialVersionUID = 8318004722887314516L;

    public FailScreen(String text, String image) {
        this.text = text;
        this.image = image;
    }

    @Override
    public boolean isOver() {
        return false;
    }

    @Override
    public void enter(World world) {

    }

    @Override
    public Screen getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        if (container.getInput().isKeyPressed(Input.KEY_ENTER)) {
            world.setGameOver(true);
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        final Image image1 = ResourceManager.getInstance().getImage(image);
        graphics.drawImage(image1, 128, 64);
        graphics.setColor(Color.yellow);
        graphics.drawRect(128, 64, image1.getWidth(), image1.getHeight());
        EngineUtils.drawRectWithBorderAndText(graphics, textRect, camera, Color.yellow, GUIConstants.backgroundColor, text, GUIConstants.dialogFont, Color.white, false);
    }

    public static FailScreen createShipDestroyedFailScreen() {
        return new FailScreen("Your ship was lost in space, a lifeless hulk flying through the void. Noone on Earth never learned what happend to you.", "ship_destroyed_gameover");
    }

    public static FailScreen createRetirementFailScreen() {
        return new FailScreen("You have been removed from commanding your ship and from Aurora project itself. \n" +
                "        No one blamed you for this failure, and your next assignments were good enough, fine ships under your command, but each time you saw Aurora ship on your radar, returning from deep space, you felt sad and some times envy...", "retirement_gameover");
    }
}
