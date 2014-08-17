package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.util.pathfinding.AStarPathFinder;
import org.newdawn.slick.util.pathfinding.Path;
import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.equip.LandingPartyWeapon;
import ru.game.aurora.world.planet.LandingParty;
import ru.game.aurora.world.planet.MonsterBehaviour;

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

    private static AStarPathFinder pathFinder;

    private static final int MAX_PATH_LENGTH = 100;
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
    }

    public static void resetPathfinder(ITileMap map) {
        pathFinder = new AStarPathFinder(map, Math.min(MAX_PATH_LENGTH, map.getWidthInTiles() / 3), false);
    }

    private Effect playAttackEffects(World world, IMovable other) {

        ResourceManager.getInstance().getSound(weapon.getShotSound()).play();
        Effect rz;
        if (weapon.getId().equals("melee")) {
            rz = new ExplosionEffect(other.getX(), other.getY(), "slash", false, false);

        } else {
            rz = new BlasterShotEffect(myMonster, other, world.getCamera(), 800, weapon);
            if (weapon.getShotSound() != null) {
                rz.setStartSound(weapon.getShotSound());
            }
        }
        world.getCurrentDungeon().getController().addEffect(rz);
        return rz;
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
            final LandingParty party = world.getPlayer().getLandingParty();
            int partyX = party.getX();
            int partyY = party.getY();

            if (myMonster.getBehaviour() == MonsterBehaviour.AGGRESSIVE) {
                ////////////////////////// attack landing party //////////////////////////////////

                final double distance = map.isWrapped() ? party.getDistanceWrapped(myMonster, map.getWidthInTiles(), map.getHeightInTiles()) : party.getDistance(myMonster);
                if (weapon != null && distance < 1.5 * weapon.getRange()) { //1.5 because of diagonal cells
                    if (!map.lineOfSightExists(x, y, partyX, partyY)) {
                        // can't shoot because no line of sight
                        return;
                    }
                    Effect eff = playAttackEffects(world, party);
                    eff.setEndListener(new IStateChangeListener<World>() {
                        private static final long serialVersionUID = -7177344379777105885L;

                        @Override
                        public void stateChanged(World world) {
                            party.subtractHp(world, weapon.getDamage());
                            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.animal_attack"), myMonster.getName(), weapon.getDamage(), party.getHp()));
                        }
                    });

                    newX = x;
                    newY = y;
                } else if (map.lineOfSightExists(x, y, partyX, partyY)
                        && (weapon == null
                        || distance < 5 * weapon.getRange())
                        || (weapon.getId().equals("melee") && distance < 15)) {
                    lastX = partyX;
                    lastY = partyY;
                    playerShown = true;
                }

                if (playerShown) {
                    // rebuild path only if player moved
                    if (path == null || lastX != partyX || lastY != partyY) {
                        lastX = partyX;
                        lastY = partyY;
                        map.setTilePassable(x, y, true);    //hack (pathfinder cannot find path if starting point is blocked)
                        path = pathFinder.findPath(null, x, y, lastX, lastY);
                        map.setTilePassable(x, y, false);   //hack
                        pathIndex = 1;
                    }

                    playerShown = false;
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
            } else if (myMonster.getBehaviour() == MonsterBehaviour.FRIENDLY && myMonster.getWeapon() != null) {
                // find some AGGRESSIVE target nearby
                List<GameObject> tmpList = new ArrayList<>(map.getObjects());
                for (GameObject po : tmpList) {

                    if (!po.canBeAttacked() || !DungeonMonster.class.isAssignableFrom(po.getClass())) {
                        continue;
                    }
                    final DungeonMonster po1 = (DungeonMonster) po;

                    if (po1.getBehaviour() != MonsterBehaviour.AGGRESSIVE) {
                        continue;
                    }

                    if (po1.getDistance(myMonster) < weapon.getRange() && map.lineOfSightExists(x, y, po.getX(), po.getY())) {

                        Effect eff = playAttackEffects(world, po1);
                        eff.setEndListener(new IStateChangeListener<World>() {
                            private static final long serialVersionUID = 995534841614292836L;

                            @Override
                            public void stateChanged(World world) {
                                po1.onAttack(world, myMonster, weapon.getDamage());
                            }
                        });
                    }
                }
                // do not move
                return;
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
                if (myMonster.getBehaviour() != MonsterBehaviour.FRIENDLY) {
                    // hack: friendly monsters do not block path
                    map.setTilePassable(myMonster.getTargetX(), myMonster.getTargetY(), false);
                }
            }

        }
    }
}
