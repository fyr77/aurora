package ru.game.aurora.player.earth.upgrades;

import ru.game.aurora.player.earth.EarthUpgrade;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.upgrades.MedBayUpgrade;
import ru.game.aurora.world.World;

/**
 * Created by Егор on 31.08.2015.
 * Unlocks super medbay upgrade, removes the previous one
 */
public class AdvancedMedbayUnlocker extends EarthUpgrade {

    @Override
    public void unlock(World world) {
        super.unlock(world);
        world.getPlayer().getEarthState().getAvailableUpgrades().remove(new MedBayUpgrade(false));
        world.getPlayer().getEarthState().getAvailableUpgrades().add(new MedBayUpgrade(true));
        world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "news_sender", "super_medbay_unlocked", "news"));
    }
}
