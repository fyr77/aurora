/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.12
 * Time: 14:39
 */
package ru.game.aurora.world.space;

import de.matthiasmann.twl.Widget;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.npc.Dialog;
import ru.game.aurora.world.World;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;

/**
 * Planet inhabited with aliens.
 * Such planet can not be landed on, only communicated
 */
public class AlienHomeworld extends BasePlanet {
    private AlienRace ownerRace;

    private Dialog dialog;

    private String spriteName;

    public AlienHomeworld(String spriteName, AlienRace ownerRace, Dialog customDialog, int size, int y, StarSystem owner, PlanetAtmosphere atmosphere, int x, PlanetCategory cat) {
        super(size, y, owner, atmosphere, x, cat);
        this.ownerRace = ownerRace;
        this.dialog = customDialog;
        this.spriteName = spriteName;

    }


    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics graphics, Camera camera, int tileX, int tileY) {
        if (!camera.isInViewport(globalX, globalY)) {
            return;
        }
        Image img = ResourceManager.getInstance().getImage(spriteName);
        graphics.drawImage(img, camera.getXCoord(globalX) - img.getWidth() / 2 + camera.getTileWidth() / 2, camera.getYCoord(globalY) - img.getHeight() / 2 + camera.getTileHeight() / 2);
    }

    @Override
    public void enter(World world) {
        world.setCurrentDialog(dialog);
    }

    @Override
    public Widget getGUI() {
        return null;
    }

    @Override
    public void update(GameContainer container, World world) {
        world.setCurrentRoom(owner);
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera) {
        // nothing
    }

    public AlienRace getOwnerRace() {
        return ownerRace;
    }
}
