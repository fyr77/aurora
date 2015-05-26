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
import ru.game.aurora.dialog.SaveFlagsDialogListener;
import ru.game.aurora.npc.AlienRace;
import ru.game.aurora.world.World;
import ru.game.aurora.world.generation.humanity.HumanityGenerator;
import ru.game.aurora.world.planet.BasePlanet;
import ru.game.aurora.world.planet.PlanetAtmosphere;
import ru.game.aurora.world.planet.PlanetCategory;
import ru.game.aurora.world.planet.PlanetSpriteGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 * Planet inhabited with aliens.
 * Such planet can not be landed on, only communicated
 */
public class AlienHomeworld extends BasePlanet {
    private static final long serialVersionUID = -1818088878360415965L;

    private final AlienRace ownerRace;

    private Dialog dialog;

    private final String spriteName;

    private boolean canBeCommunicated = true;

    private Map<String, String> dialogFlags = null;

    private transient Image planetImage = null;

    public AlienHomeworld(String spriteName, AlienRace ownerRace, Dialog customDialog, int size, int y, StarSystem owner, PlanetAtmosphere atmosphere, int x, PlanetCategory cat) {
        super(x, y, size, owner, atmosphere, cat);
        this.ownerRace = ownerRace;
        this.dialog = customDialog;
        this.spriteName = spriteName;
    }


    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics graphics, Camera camera, int tileX, int tileY) {
        if ((planetImage == null) || x != oldX || y != oldY) {
            planetImage = ResourceManager.getInstance().getImage(spriteName);
            double theta = Math.atan2(y, x);
            float shadowXFactor = (float) (0.5 - Math.cos(theta) * 0.25);
            float shadowYFactor = (float) (0.5 - Math.sin(theta) * 0.25);
            planetImage = PlanetSpriteGenerator.shadowPlanet(planetImage, shadowXFactor, shadowYFactor);
            oldX = x;
            oldY = y;
        }
        graphics.drawImage(planetImage, camera.getXCoord(x) - planetImage.getWidth() / 2 + camera.getTileWidth() / 2, camera.getYCoord(y) - planetImage.getHeight() / 2 + camera.getTileHeight() / 2);
    }

    @Override
    public void enter(World world) {
        if (!canBeCommunicated) {
            return;
        }
        if (dialogFlags == null) {
            dialogFlags = new HashMap<>();
            dialog.addListener(new SaveFlagsDialogListener(dialogFlags));
        }
        dialogFlags.put("reputation", String.valueOf(world.getReputation().getReputation(ownerRace.getName(), HumanityGenerator.NAME)));
        world.addOverlayWindow(dialog, dialogFlags);

    }

    @Override
    public void returnTo(World world) {
        enter(world);
    }

    @Override
    public void update(GameContainer container, World world) {
        world.setCurrentRoom(owner);
    }

    @Override
    public void draw(GameContainer container, Graphics graphics, Camera camera, World world) {
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
    public String getScanText() {
        return Localization.getText("races", getOwnerRace().getName() + ".homeworld.description");
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public void setCanBeCommunicated(boolean canBeCommunicated) {
        this.canBeCommunicated = canBeCommunicated;
    }

    @Override
    public Image getImage() {
        return planetImage;
    }

    @Override
    public String getName() {
        return getFaction().getName() + " homeworld";
    }
    
    @Override
    public boolean canBeCommunicated() {
        return canBeCommunicated;
    }
}
