/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 24.12.12
 * Time: 14:50
 */
package ru.game.aurora.world.planet;

import org.newdawn.slick.GameContainer;
import ru.game.aurora.player.Player;
import ru.game.aurora.world.Room;
import ru.game.aurora.world.space.GalaxyMapObject;
import ru.game.aurora.world.space.StarSystem;


public abstract class BasePlanet implements Room, GalaxyMapObject {
    private static final long serialVersionUID = 5047939414473391148L;
    protected StarSystem owner;
    protected PlanetCategory category;
    protected PlanetAtmosphere atmosphere;
    /**
     * Planet size type. 1 is largest, 4 is smallest.
     * Planet image size on global map and dimensions of planet surface depends on it.
     */
    protected int size;
    /**
     * Position of planet in star system
     */
    protected int globalX;
    protected int globalY;

    public BasePlanet(int size, int y, StarSystem owner, PlanetAtmosphere atmosphere, int x, PlanetCategory cat) {
        this.size = size;
        this.globalY = y;
        this.owner = owner;
        this.atmosphere = atmosphere;
        this.globalX = x;
        this.category = cat;
    }

    public int getGlobalX() {
        return globalX;
    }

    public int getGlobalY() {
        return globalY;
    }

    @Override
    public boolean canBeEntered() {
        return true;
    }

    @Override
    public void processCollision(GameContainer container, Player player) {
    }

    public void setGlobalY(int globalY) {
        this.globalY = globalY;
    }

    public void setGlobalX(int globalX) {
        this.globalX = globalX;
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
        return null;
    }

    public StarSystem getOwner() {
        return owner;
    }

    public int getSize() {
        return size;
    }

    public boolean hasLife()
    {
        return false;
    }
}
