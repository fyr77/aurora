package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.LandingParty;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 25.10.13
 * Time: 14:11
 */
public class MonsterController implements Serializable {
    private static final long serialVersionUID = -8278864000200488198L;

    private IMonster myMonster;

    private int turnsBeforeMove;

    private LandingPartyWeapon weapon;

    private ITileMap map;

    private transient AStarPathFinder pathFinder;
    private Path path;
    private int lastX;
    private int lastY;
    private boolean playerShown = false;
    private int pathIndex = 1;

    public MonsterController(ITileMap map, IMonster myMonster) {
        this.map = map;
        this.myMonster = myMonster;
        this.turnsBeforeMove = myMonster.getSpeed();
        this.weapon = myMonster.getWeapon();

        pathFinder = new AStarPathFinder(map, 200, false);   //200 - макс путь
    }

    public void update(GameContainer container, World world) {
        if (myMonster.nowMoving()) {
            return;
        }
        if (myMonster.getHp() <= 0) {
            return;
        }
        if (!world.isUpdatedThisFrame()) {
            return;
        }
        if (--turnsBeforeMove == 0) {
            turnsBeforeMove = myMonster.getSpeed();
            final int x = myMonster.getX();
            final int y = myMonster.getY();

            int newX = x + CommonRandom.getRandom().nextInt(2) - 1;
            int newY = y + CommonRandom.getRandom().nextInt(2) - 1;
            // if we want to attack landing party and it is close enough, move closer

            LandingParty party = world.getPlayer().getLandingParty();
            int partyX = party.getX();
            int partyY = party.getY();

            final double distance = map.isWrapped() ? party.getDistanceWrapped(myMonster, map.getWidthInTiles(), map.getHeightInTiles()) : party.getDistance(myMonster);
            if (distance < 1.5 * weapon.getRange()) { //1.5 because of diagonal cells
                if (!map.lineOfSightExists(x, y, partyX, partyY)) {
                    // can't shoot because no line of sight
                    return;
                }
                party.subtractHp(world, weapon.getDamage());
                GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.animal_attack"), myMonster.getName(), weapon.getDamage(), party.getHp()));
                if (weapon.getId().equals("melee")) {
                    world.getCurrentDungeon().getController().setCurrentEffect(new ExplosionEffect(partyX, partyY, "slash", false, false));
                } else {
                    world.getCurrentDungeon().getController().setCurrentEffect(new BlasterShotEffect(myMonster, party, world.getCamera(), 800, weapon));
                }
                ResourceManager.getInstance().getSound(weapon.getShotSound()).play();
                newX = x;
                newY = y;
            } else if (distance < 5 * weapon.getRange() && map.lineOfSightExists(x, y, partyX, partyY)) {
                lastX = partyX;
                lastY = partyY;
                playerShown = true;
            }

            if (playerShown) {
                if (pathFinder == null) {
                    pathFinder = new AStarPathFinder(map, 200, false);
                }

                map.setTilePassable(x, y, true);    //hack (pathfinder cannot find path if starting point is blocked)
                path = pathFinder.findPath(null, x, y, lastX, lastY);
                map.setTilePassable(x, y, false);   //hack

                playerShown = false;
                pathIndex = 1;
            }

            if (path != null && path.getLength() > 1) {
                newX = path.getX(pathIndex);
                newY = path.getY(pathIndex);
                pathIndex++;
                if (pathIndex >= (path.getLength() - 1)) {
                    path = null;
                }
            }

            if (map.isTilePassable(newX, newY)) {
                map.setTilePassable(x, y, true);
                if (newX > x) {
                    myMonster.moveRight();
                } else if (newX < x) {
                    myMonster.moveLeft();
                } else if (newY < y) {
                    myMonster.moveUp();
                } else if (newY > y) {
                    myMonster.moveDown();
                }
                map.setTilePassable(myMonster.getTargetX(), myMonster.getTargetY(), false);
            }

        }
    }
}
