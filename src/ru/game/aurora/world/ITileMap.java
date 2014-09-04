/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 07.09.13
 * Time: 22:21
 */
package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.TileBasedMap;
import rlforj.los.ILosBoard;
import ru.game.aurora.application.Camera;
import ru.game.aurora.world.dungeon.IVictoryCondition;
import ru.game.aurora.world.planet.LandingParty;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Interface for different tile map formats.
 */
public interface ITileMap extends Serializable, ILosBoard, TileBasedMap {
    public List<GameObject> getObjects();

    public void draw(GameContainer container, Graphics graphics, Camera camera);

    public boolean isTilePassable(int x, int y);

    public boolean isTilePassable(LandingParty landingParty, int x, int y);

    public boolean isTileVisible(int x, int y);

    public boolean lineOfSightExists(int x1, int y1, int x2, int y2);

    /**
     * Returns true if map is wrapped - so moving over it left border makes you appear at the right border. Like a globe.
     */
    public boolean isWrapped();

    /**
     * Updates planet map. Makes tiles visible in given range from given point
     *
     * @return Amount of tiles opened
     */
    public int updateVisibility(int x, int y, int range);

    public void setTilePassable(int x, int y, boolean isPassable);

    /**
     * Get a spawn point for a landing party
     */
    public BasePositionable getEntryPoint();

    public Collection<BasePositionable> getExitPoints();

    public Collection<IVictoryCondition> getVictoryConditions();

    public AStarPathFinder getPathFinder();
}
