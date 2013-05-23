/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 30.01.13
 * Time: 16:12
 */

package ru.game.aurora.gui;

import de.lessvoid.nifty.screen.Screen;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.geom.Rectangle;
import ru.game.aurora.application.Camera;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.OverlayWindow;
import ru.game.aurora.world.World;

public class HelpScreen implements OverlayWindow
{
    private static final long serialVersionUID = 8162960879655162640L;

    private boolean isOver = false;

    private static final Rectangle rect = new Rectangle(1, 1, 10, 8);

    private static final String helpText = "Controls: \n " +
            "Starship mode: \n " +
            "Arrows - move \n " +
            "Enter - interact (hail ships, land on planets, enter star systems) \n " +
            "E - engineering screen (repairs and ship upgrades) \n " +
            "R - research screen \n " +
            "M - global star map (only when outside of star system) \n " +
            "Number keys - fire weapon at slot with that number (will switch to target selection mode) \n " +
            " \n " +
            " \n " +
            "Planet mode: \n " +
            "Arrows - move \n " +
            "Enter - interact (collect items, excavate resources) \n " +
            "F - toggle firing mode \n " +
            " \n " +
            " \n " +
            "To leave star system, cross red line on its border \n " +
            "To leave planet, move to a tile with shuttle and press <enter> \n " +
            " \n " +
            "Game will be over if your ship is destroyed, or if you return to Earth with too little science data. Explore planets," +
            "space and artifacts to collect more data." +
            " \n " +
            "Press <enter> to continue";


    @Override
    public boolean isOver() {
        return isOver;
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
        if (container.getInput().isKeyPressed(Input.KEY_ENTER) || container.getInput().isKeyPressed(Input.KEY_ESCAPE)) {
            isOver = true;
        }
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        EngineUtils.drawRectWithBorderAndText(graphics, rect, camera, helpText);
    }
}
