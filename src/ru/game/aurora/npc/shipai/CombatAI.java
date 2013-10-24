/**
 * User: jedi-philosopher
 * Date: 26.01.13
 * Time: 20:23
 */
package ru.game.aurora.npc.shipai;

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
                if (weapon.getReloadTimeLeft() <= 0) {
                    ship.fire(world, currentSystem, i, target);
                }
            }
        }
        // not all weapons fired because too far, move closer
        if (inRange < ship.getWeapons().length) {
            if (target.getX() < ship.getX()) {
                ship.move(- 1, 0);
            } else if (target.getX() > ship.getX()) {
                ship.move(1, 0);
            } else if (target.getY() < ship.getY()) {
                ship.move(0, - 1);
            } else if (target.getY() > ship.getY()) {
                ship.move(0, 1);
            }
        }

    }

    @Override
    public boolean isAlive() {
        return true;
    }
}
