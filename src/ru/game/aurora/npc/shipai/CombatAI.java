/**
 * User: jedi-philosopher
 * Date: 26.01.13
 * Time: 20:23
 */
package ru.game.aurora.npc.shipai;

import ru.game.aurora.application.CommonRandom;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;
import ru.game.aurora.world.space.NPCShip;
import ru.game.aurora.world.space.SpaceObject;
import ru.game.aurora.world.space.StarSystem;

/**
 * Simple AI that will follow its target and attack with all possible weapons
 */
public class CombatAI implements NPCShipAI {
    private static final long serialVersionUID = -8406568455196600324L;

    private SpaceObject target;

    public CombatAI(SpaceObject target) {
        this.target = target;
    }

    @Override
    public void update(NPCShip ship, World world, StarSystem currentSystem) {
        if (ship.getWeapons() == null || ship.getWeapons().length == 0 || !target.isAlive()) {
            // no weapons / no target - flee
            ship.setAi(new LeaveSystemAI());
            return;
        }

        double distance = ship.getDistance(target);

        int inRange = 0;
        for (int i = 0; i < ship.getWeapons().length; ++i) {
            final StarshipWeapon weapon = ship.getWeapons()[i];
            if (weapon.getWeaponDesc().range >= distance) {
                inRange++;
            }
        }

        // more weapons in range - more chance to shoot
        if (inRange > 0 && ship.isStationary() || CommonRandom.getRandom().nextDouble() < (float)inRange / ship.getWeapons().length) {
            fireAtTarget(ship, world, currentSystem, distance);
            return;
        }
        if (ship.isStationary()) {
            return;
        }

        // hack: if too much aggro - we are being focused by multiple enemies, move away
        int val = 1;
        int aggroSum = 0;
        for (Integer i : ship.getThreatMap().values()) {
            aggroSum += i;
        }

        if (aggroSum > ship.getHp() * 2) {
            val = -1;
        }

        // not all weapons fired because too far, move closer
        // if all weapons are in range, move closer in a random fashion, so that not all ships gather on single tile
        if (inRange < ship.getWeapons().length || (distance > 1 && CommonRandom.getRandom().nextBoolean())) {
            // randomly move either on X, or on Y, so that all ships do not get grouped on same path within 1-2 tiles
             if (CommonRandom.getRandom().nextBoolean()) {
                 if (!moveToTargetOnX(val, ship)) {
                     moveToTargetOnY(val, ship);
                 }
             } else {
                 if (!moveToTargetOnY(val, ship)) {
                     moveToTargetOnX(val, ship);
                 }
             }
        }

    }

    private boolean moveToTargetOnX(int val, NPCShip ship)
    {
        if (val * target.getX() < val * ship.getX()) {
            ship.moveLeft();
            return true;
        } else if (val * target.getX() > val * ship.getX()) {
            ship.moveRight();
            return true;
        }
        return false;
    }

    private boolean moveToTargetOnY(int val, NPCShip ship)
    {
        if (val * target.getY() < val * ship.getY()) {
            ship.moveUp();
            return true;
        } else if (val * target.getY() > val * ship.getY()) {
            ship.moveDown();
            return true;
        }
        return false;
    }

    private void fireAtTarget(NPCShip ship, World world, StarSystem currentSystem, double distance) {
        for (int i = 0; i < ship.getWeapons().length; ++i) {
            final StarshipWeapon weapon = ship.getWeapons()[i];
            if (weapon.getReloadTimeLeft() <= 0 && weapon.getWeaponDesc().range >= distance) {
                ship.fire(world, currentSystem, i, target);
            }
        }
    }

    @Override
    public boolean isAlive() {
        return true;
    }
}
