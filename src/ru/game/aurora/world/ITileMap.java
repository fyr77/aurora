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
import ru.game.aurora.effects.Effect;
import ru.game.aurora.world.dungeon.IVictoryCondition;
import ru.game.aurora.world.planet.LandingParty;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Interface for different tile map formats.
 */
public interface ITileMap extends Serializable, ILosBoard, TileBasedMap {
    List<GameObject> getObjects();

    Collection<Effect> getEffects();

    void draw(GameContainer container, Graphics graphics, Camera camera);

    boolean isTilePassable(int x, int y);

    boolean isTilePassable(LandingParty landingParty, int x, int y);

    boolean isTileVisible(int x, int y);

    boolean lineOfSightExists(int x1, int y1, int x2, int y2);

    /**
     * Returns true if map is wrapped - so moving over it left border makes you appear at the right border. Like a globe.
     */
    boolean isWrapped();

    /**
     * Updates planet map. Makes tiles visible in given range from given point
     *
     * @return Amount of tiles opened
     */
    int updateVisibility(int x, int y, int range);

    void setTilePassable(int x, int y, boolean isPassable);

    /**
     * Get a spawn point for a landing party
     */
    BasePositionable getEntryPoint();

    Collection<IVictoryCondition> getVictoryConditions();

    AStarPathFinder getPathFinder();

    Object getUserData();

    void setUserData(Object o);
}
