/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 23.07.13
 * Time: 16:07
 */
package ru.game.aurora.player.engineering.projects;

import ru.game.aurora.application.GameLogger;
import ru.game.aurora.application.Localization;
import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.player.engineering.EngineeringProject;
import ru.game.aurora.world.Ship;
import ru.game.aurora.world.World;
import ru.game.aurora.world.equip.StarshipWeapon;


public class AdvancedLasers extends EngineeringProject {
    private static final long serialVersionUID = -6929677248004188022L;

    public AdvancedLasers() {
        super("advanced_lasers", "ship_laser2");
        remainingProgress = 10;
    }

    @Override
    public void onCompleted(World world) {
        Ship ship = world.getPlayer().getShip();
        GameLogger.getInstance().logMessage(Localization.getText("engineering", "advanced_lasers.end_message"));
        for (int idx = 0; idx < ship.getWeapons().size(); ++idx) {
            StarshipWeapon sw = ship.getWeapons().get(idx);
            if (sw.getWeaponDesc().getId().equals("laser_cannon")) {
                ship.getWeapons().set(idx, new StarshipWeapon(ResourceManager.getInstance().getWeapons().getEntity("laser_cannon2"), sw.getMountPosition()));
            }
        }
    }
}
