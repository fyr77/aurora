package ru.game.aurora.world.space.earth;

import ru.game.aurora.application.ResourceManager;
import ru.game.aurora.dialog.Dialog;
import ru.game.aurora.dialog.DialogListener;
import ru.game.aurora.player.earth.PrivateMessage;
import ru.game.aurora.player.engineering.upgrades.WeaponUpgrade;
import ru.game.aurora.world.GameEventListener;
import ru.game.aurora.world.World;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Egor.Smirnov
 * Date: 14.05.14
 * Time: 18:09
 */
public class EarthUpgradeUnlocker extends GameEventListener {
    private static final long serialVersionUID = 3886247909156678459L;

    private int prevTechValue = 0;

    @Override
    public boolean onReturnToEarth(World world) {
        final int technologyLevel = world.getPlayer().getEarthState().getTechnologyLevel();

        if (technologyLevel > 50 && prevTechValue <= 50) {
            world.getPlayer().getEarthState().getMessages().add(new PrivateMessage(world, "letters.boy.sender", "letters.boy", "message"));
        }

        if (technologyLevel > 450 && prevTechValue <= 450) {

            // unlock missiles
            Dialog d = Dialog.loadFromFile("dialogs/missiles_unlocked.json");
            d.addListener(new DialogListener() {
                private static final long serialVersionUID = 6793777309983886452L;

                @Override
                public void onDialogEnded(World world, Dialog dialog, int returnCode, Map<String, String> flags) {
                    WeaponUpgrade upgrade = new WeaponUpgrade(ResourceManager.getInstance().getWeapons().getEntity("humanity_missiles"));
                    world.getPlayer().getEarthState().getAvailableUpgrades().add(upgrade);
                }
            });

            world.getPlayer().getEarthState().getEarthSpecialDialogs().add(d);
        }


        prevTechValue = technologyLevel;
        return false;
    }
}
