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
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.PlanetObject;
import ru.game.aurora.world.planet.nature.AnimalSpeciesDesc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    private void playAttackEffects(World world, IMovable other) {

        if (weapon.getId().equals("melee")) {
            world.getCurrentDungeon().getController().addEffect(new ExplosionEffect(other.getX(), other.getY(), "slash", false, false));
        } else {
            world.getCurrentDungeon().getController().addEffect(new BlasterShotEffect(myMonster, other, world.getCamera(), 800, weapon));
        }
        ResourceManager.getInstance().getSound(weapon.getShotSound()).play();
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

            if (myMonster.getBehaviour() == AnimalSpeciesDesc.Behaviour.AGGRESSIVE) {
                ////////////////////////// attack landing party //////////////////////////////////
                LandingParty party = world.getPlayer().getLandingParty();
                int partyX = party.getX();
                int partyY = party.getY();

                final double distance = map.isWrapped() ? party.getDistanceWrapped(myMonster, map.getWidthInTiles(), map.getHeightInTiles()) : party.getDistance(myMonster);
                if (weapon != null && distance < 1.5 * weapon.getRange()) { //1.5 because of diagonal cells
                    if (!map.lineOfSightExists(x, y, partyX, partyY)) {
                        // can't shoot because no line of sight
                        return;
                    }
                    party.subtractHp(world, weapon.getDamage());
                    GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.animal_attack"), myMonster.getName(), weapon.getDamage(), party.getHp()));
                    playAttackEffects(world, party);
                    newX = x;
                    newY = y;
                } else if (map.lineOfSightExists(x, y, partyX, partyY) && (weapon == null || distance < 5 * weapon.getRange())) {
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

                /////////////////////////////////////////////////////////////////////////
            } else if (myMonster.getBehaviour() == AnimalSpeciesDesc.Behaviour.FRIENDLY && myMonster.getWeapon() != null) {
                // find some AGGRESSIVE target nearby
                List<PlanetObject> tmpList = new ArrayList<>(map.getObjects());
                for (PlanetObject po : tmpList) {

                    if (!po.canBeShotAt() || !DungeonMonster.class.isAssignableFrom(po.getClass())) {
                        continue;
                    }
                    DungeonMonster po1 = (DungeonMonster) po;

                    if (po1.getBehaviour() != AnimalSpeciesDesc.Behaviour.AGGRESSIVE) {
                        continue;
                    }

                    if (po1.getDistance(myMonster) < weapon.getRange() && map.lineOfSightExists(x, y, po.getX(), po.getY())) {
                        po1.onShotAt(world, weapon.getDamage());
                        playAttackEffects(world, po1);
                    }
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
                if (myMonster.getBehaviour() != AnimalSpeciesDesc.Behaviour.FRIENDLY) {
                    // hack: friendly monsters do not block path
                    map.setTilePassable(myMonster.getTargetX(), myMonster.getTargetY(), false);
                }
            }

        }
    }
}
