/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.12
 * Time: 14:50
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import ru.game.aurora.application.*;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.World;
import ru.game.aurora.world.space.BaseSpaceObject;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;

import java.util.ArrayList;
import java.util.List;


public abstract class BasePlanet extends BaseSpaceObject implements Room, GalaxyMapObject {
    private static final long serialVersionUID = 1L;
    protected StarSystem owner;
    protected PlanetCategory category;
    protected PlanetAtmosphere atmosphere;
    /**
     * If planet has rings, this value is non-zero index of rings sprite
     */
    protected int rings;
    /**
     * Planet size type. 1 is largest, 4 is smallest.
     * Planet image size on global map and dimensions of planet surface depends on it.
     */
    protected int size;

    protected List<BasePlanet> satellites;

    private transient Image sprite;

    public BasePlanet(int x, int y, int size, StarSystem owner, PlanetAtmosphere atmosphere, PlanetCategory cat) {
        super(x, y);
        this.size = size;
        this.owner = owner;
        this.atmosphere = atmosphere;
        this.category = cat;
    }

    public void setRings(int ringsId) {
        this.rings = ringsId;
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    @Override
    public void processCollision(GameContainer container, Player player) {
    }

    /**
     * True if this is ordinary planet that can be explored with landing party.
     * False if this is some kind of quest location that can not be actually landed on (like alien homeworld)
     */
    public boolean canBeLanded() {
        return true;
    }

    public PlanetCategory getCategory() {
        return category;
    }

    public PlanetAtmosphere getAtmosphere() {
        return atmosphere;
    }

    @Override
    public String getName() {
        return "Planet";
    }

    public StarSystem getOwner() {
        return owner;
    }

    public int getSize() {
        return size;
    }

    public boolean hasLife() {
        return false;
    }

    public void addSatellite(BasePlanet p) {
        if (satellites == null) {
            satellites = new ArrayList<>();
        }

        int orbit = satellites.size() + 2;
        int satelliteX = CommonRandom.getRandom().nextInt(2 * orbit) - orbit;

        int satelliteY = (int) (Math.sqrt(orbit * orbit - satelliteX * satelliteX) * (CommonRandom.getRandom().nextBoolean() ? -1 : 1));
        p.setPos(x + satelliteX, y + satelliteY);
        satellites.add(p);
    }

    public List<BasePlanet> getSatellites() {
        return satellites;
    }


    public String getScanText() {
        return "";
    }

    @Override
    public void drawOnGlobalMap(GameContainer container, Graphics graphics, Camera camera, int tileX, int tileY) {
        if ((sprite == null) || x != oldX || y != oldY) {
            sprite = PlanetSpriteGenerator.getInstance().createPlanetSprite(camera, this);
            oldX = x;
            oldY = y;
        }
        // draw planetary rings in 2 steps - first part that is behind planet, then planet itself, then part before planet
        // draw rings shifted on 1/10 of planet diameter - looks nicer
        if (rings != 0) {
            Image backRingsSprite = ResourceManager.getInstance().getImage("ring_back_" + rings);
            graphics.drawImage(backRingsSprite, camera.getXCoord(x) + (camera.getTileWidth() - backRingsSprite.getWidth()) / 2, camera.getYCoord(y) + (camera.getTileHeight() - backRingsSprite.getHeight()) / 2);
        }
        graphics.drawImage(sprite, camera.getXCoord(x) + (camera.getTileWidth() - sprite.getWidth()) / 2, camera.getYCoord(y) + (camera.getTileHeight() - sprite.getHeight()) / 2);

        if (rings != 0) {
            Image frontRingsSprite = ResourceManager.getInstance().getImage("ring_front_" + rings);
            graphics.drawImage(frontRingsSprite, camera.getXCoord(x) + (camera.getTileWidth() - frontRingsSprite.getWidth()) / 2, camera.getYCoord(y) + (camera.getTileHeight() - frontRingsSprite.getHeight()) / 2);
        }
    }

    @Override
    public Image getImage() {
        return sprite;
    }

    @Override
    public void onContact(World world) {
        if (category == PlanetCategory.GAS_GIANT) {
            GameLogger.getInstance().logMessage(Localization.getText("gui", "space.can_not_land"));
            return;
        }
        GameLogger.getInstance().logMessage(Localization.getText("gui", "landing"));
        world.setCurrentRoom(this);
        enter(world);
    }
}
