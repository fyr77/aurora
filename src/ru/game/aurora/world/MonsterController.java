package ru.game.aurora.world;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.util.pathfinding.Path;
import ru.game.aurora.application.*;
import ru.game.aurora.effects.BlasterShotEffect;
import ru.game.aurora.effects.Effect;
import ru.game.aurora.effects.ExplosionEffect;
import ru.game.aurora.util.EngineUtils;
import ru.game.aurora.world.dungeon.DungeonMonster;
import ru.game.aurora.world.equip.WeaponDesc;
import ru.game.aurora.world.equip.WeaponInstance;
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

    private final IMonster myMonster;

    private int turnsBeforeMove;

    private final WeaponInstance weapon;

    private final ITileMap map;

    private Path path;
    private int lastX;
    private int lastY;
    private boolean playerShown = false;
    private int pathIndex = 1;

    public MonsterController(ITileMap map, IMonster myMonster) {
        this.map = map;
        this.myMonster = myMonster;
        this.turnsBeforeMove = myMonster.getSpeed();
        weapon = !myMonster.getWeapons().isEmpty() ? myMonster.getWeapons().get(0) : null;
    }

    private Effect playAttackEffects(World world, IMovable other, WeaponDesc weapon) {
        ResourceManager.getInstance().getSound(weapon.getShotSound()).play();
        Effect rz;
        if (weapon.getId().equals("melee")) {
            rz = new ExplosionEffect(other.getX(), other.getY(), "slash", false, false);

        } else {
            final Camera camera = world.getCamera();
            float targetScreenX;
            float targetScreenY;

            if (map.isWrapped()) {
                targetScreenX = camera.getXCoordWrapped(other.getX(), map.getWidthInTiles());
                targetScreenY = camera.getYCoordWrapped(other.getY(), map.getHeightInTiles());
            } else {
                targetScreenX = camera.getXCoord(other.getX());
                targetScreenY = camera.getYCoord(other.getY());
            }
            targetScreenX += other.getOffsetX() + camera.getTileWidth() / 2;
            targetScreenY += other.getOffsetY() + camera.getTileHeight() / 2;
            rz = new BlasterShotEffect(myMonster, targetScreenX, targetScreenY, camera, 800, weapon, map);
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

        if (weapon != null) {
            weapon.reload();
        }

        if (--turnsBeforeMove == 0) {
            turnsBeforeMove = myMonster.getSpeed();
            final int x = map.isWrapped() ? EngineUtils.wrap(myMonster.getX(), map.getWidthInTiles()) : myMonster.getX();
            final int y = map.isWrapped() ? EngineUtils.wrap(myMonster.getY(), map.getHeightInTiles()) : myMonster.getY();

            int newX = x + CommonRandom.getRandom().nextInt(2) - 1;
            int newY = y + CommonRandom.getRandom().nextInt(2) - 1;
            // if we want to attack landing party and it is close enough, move closer
            final LandingParty party = world.getPlayer().getLandingParty();
            int partyX = party.getX();
            int partyY = party.getY();

            if (myMonster.getBehaviour() == MonsterBehaviour.AGGRESSIVE) {
                ////////////////////////// attack landing party //////////////////////////////////

                final double distance = map.isWrapped() ? party.getDistanceWrapped(myMonster, map.getWidthInTiles(), map.getHeightInTiles()) : party.getDistance(myMonster);
                if (weapon != null && distance < Math.max(10, 1.5 * weapon.getWeaponDesc().getRange())) { //1.5 because of diagonal cells, 10 because melee monsters have attack range 1
                    if (!map.lineOfSightExists(x, y, partyX, partyY)) {
                        // can't shoot because no line of sight
                        return;
                    }
                    if (!weapon.isReady()) {
                        return;
                    }
                    Effect eff = playAttackEffects(world, party, weapon.getWeaponDesc());
                    eff.setEndListener(new IStateChangeListener<World>() {
                        private static final long serialVersionUID = -7177344379777105885L;

                        @Override
                        public void stateChanged(World world) {
                            party.subtractHp(world, weapon.getWeaponDesc().getDamage());
                            GameLogger.getInstance().logMessage(String.format(Localization.getText("gui", "surface.animal_attack"), myMonster.getName(), weapon.getWeaponDesc().getDamage(), party.getHp()));
                        }
                    });

                    newX = x;
                    newY = y;
                } else if (map.lineOfSightExists(x, y, partyX, partyY)
                        && (weapon == null
                        || distance < 5 * weapon.getWeaponDesc().getRange()
                        || (weapon.getWeaponDesc().getId().equals("melee") && distance < 15))) {
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
                        path = map.getPathFinder().findPath(null, x, y, lastX, lastY);
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
            } else if (myMonster.getBehaviour() == MonsterBehaviour.FRIENDLY && weapon != null) {
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

                    if (po1.getDistance(myMonster) < weapon.getWeaponDesc().getRange() && map.lineOfSightExists(x, y, po.getX(), po.getY())) {

                        Effect eff = playAttackEffects(world, po1, weapon.getWeaponDesc());
                        eff.setEndListener(new IStateChangeListener<World>() {
                            private static final long serialVersionUID = 995534841614292836L;

                            @Override
                            public void stateChanged(World world) {
                                po1.onAttack(world, myMonster, weapon.getWeaponDesc().getDamage());
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
