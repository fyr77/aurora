/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.12
 * Time: 14:39
 */
package ru.game.aurora.world.space;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.Camera;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;

import java.util.HashMap;
import java.util.Map;

/**
 * Planet inhabited with aliens.
 * Such planet can not be landed on, only communicated
 */
public class AlienHomeworld extends BasePlanet {
    private static final long serialVersionUID = -1818088878360415965L;

    private AlienRace ownerRace;

    private Dialog dialog;

    private String spriteName;

    private transient Map<String, String> dialogFlags = null;

    public AlienHomeworld(String spriteName, AlienRace ownerRace, Dialog customDialog, int size, int y, StarSystem owner, PlanetAtmosphere atmosphere, int x, PlanetCategory cat) {
        super(x, y, size, owner, atmosphere, cat);
        this.ownerRace = ownerRace;
        this.dialog = customDialog;
        this.spriteName = spriteName;
    }


    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics graphics, Camera camera, int tileX, int tileY) {
        Image img = ResourceManager.getInstance().getImage(spriteName);
        graphics.drawImage(img, camera.getXCoord(x) - img.getWidth() / 2 + camera.getTileWidth() / 2, camera.getYCoord(y) - img.getHeight() / 2 + camera.getTileHeight() / 2);
    }

    @Override
    public void enter(World world) {
        if (dialogFlags == null) {
            dialogFlags = new HashMap<>();
        }
        dialogFlags.put("reputation", String.valueOf(world.getReputation().getReputation(ownerRace.getName(), HumanityGenerator.NAME)));
        world.addOverlayWindow(dialog, dialogFlags);

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

    @Override
    public boolean canBeLanded() {
        return false;
    }

    @Override
    public StringBuilder getScanText() {
        return new StringBuilder(Localization.getText("races", getOwnerRace().getName() + ".homeworld.description"));
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }
}
